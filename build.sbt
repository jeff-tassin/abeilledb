ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"
val Http4sVersion = "1.0.0-M39"

lazy val root = (project in file("."))
  .settings(
    name := "abeille-db",
    libraryDependencies ++= Seq(
      "org.jfree" % "jfreechart" % "1.5.4",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
    ),
    fork := true
  )

Compile / mainClass := Some("com.jeta.abeille.main.Main")
Compile / resourceDirectory := baseDirectory.value / "assets"
