
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"
val Http4sVersion = "1.0.0-M39"


ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "abeille-db",
    libraryDependencies ++= Seq(
      "org.jfree" % "jfreechart" % "1.5.4",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.6-0142603"
    ),
    fork := true,
    assembly / mainClass := Some("com.jeta.abeille.main.Main"),
    assembly / assemblyJarName := "abeilledb.jar"
  )

Compile / mainClass := Some("com.jeta.abeille.main.Main")
Compile / resourceDirectory := baseDirectory.value / "assets"
Compile / packageBin / packageOptions +=
  Package.ManifestAttributes(java.util.jar.Attributes.Name.SEALED -> "true")