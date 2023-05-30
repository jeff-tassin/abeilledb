package circe

import io.circe._
import io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

case class Movie(title:String,releaseYear:Int)
case class Employee(name: String, age: Int, favoriteMovies: List[Movie])
case class Id(id: String, employee: Employee)

object CirceComplexDecodeDemo {
  def test() : Unit = {
    decodeExample3()
  }

  val employeeJson = """
    {
       "id":"101166",
       "employee":{
          "name":"Jeff",
          "age":55,
          "favoriteMovies":[
             {
                "title":"Star Wars",
                "releaseYear":1977
             },
             {
                "title":"Jaws",
                "releaseYear":1975
             }
          ]
       }
    }
  """

  /**
   * auto decoder
   */
  def decodeExample3() : Unit = {
    val idCard = decode[Id](employeeJson)
    println( idCard )
  }
}
