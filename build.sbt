ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "abeille-db",
    libraryDependencies ++= Seq(
      "org.jfree" % "jfreechart" % "1.5.4",
    ),
    fork := true
  )

Compile / mainClass := Some("com.jeta.abeille.main.Main")
Compile / resourceDirectory := baseDirectory.value / "assets"
