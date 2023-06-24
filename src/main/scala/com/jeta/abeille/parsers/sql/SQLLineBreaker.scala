package com.jeta.abeille.parsers.sql

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import com.jeta.abeille.parsers.sql.SQLLineBreaker.splitFirst

import java.io.{File, FileInputStream, FileReader}
import scala.::
import scala.io.Source
import scala.util.matching.Regex

object SQLLineBreaker {

  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make {
      IO.blocking(new FileInputStream(f))
    } { inStream =>
      IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  def readFile(path: String): IO[String] = {
    inputStream( new File(path) ).use(istream =>
      IO.blocking(
        Source.fromInputStream(istream)
          .getLines()
          .mkString
      )
    )
  }

  def splitFirst( sval:String, tokenEx:Regex ) : (String,String) = {
    tokenEx.findFirstMatchIn(sval) match {
      case Some(m) => (sval.substring(0, m.start), sval.substring(m.end))
      case None => (sval,"")
    }
  }

  val LINE_LENGTH = 120
  def wrapAll( str:String, tokenEx:Regex) : String = {
    case class Accumulator(prevIdx:Int=0, lines:Array[String]=Array())

    val accumResult = tokenEx.findAllMatchIn(str).foldLeft( Accumulator() )( (accum,m) =>
      Accumulator(
        m.end,
        accum.lines :+ str.substring( accum.prevIdx, m.start ) :+ str.substring(m.start, m.end)
      )
    )
    (accumResult.lines :+ str.substring(accumResult.prevIdx))
      .foldLeft( (0, "") )((accum, line) => {
        if ( line.length > LINE_LENGTH ||  line.length + accum._1 > LINE_LENGTH )
          (0, accum._2 + "\n" + line)
        else
          ( accum._1 + line.length, accum._2 + line )
      })._2
  }

  def wrapAll(str: String, token: Array[String]): String = {
    val regex = s"(?i)(${token.mkString("|")})".r
    regex.replaceAllIn(str,m => s"\n${m.matched}")
  }


  private def processInternal(str: String) : String = {
    var (cols, from) = splitFirst( str, "(?i)FROM".r )
    if ( splitFirst( from, "(?i)FROM".r )._2 != "" ) {
      from = processInternal(from)
    }
    Array(
      wrapAll( cols, "(?i),".r),
      "FROM" + wrapAll( from, Array("INNER JOIN ", "LEFT OUTER ", "WHERE "))
    ).mkString("\n")
  }

  def process(str: String) : String = {
    "\\n+".r.replaceAllIn( processInternal(str), "\n")
  }
}