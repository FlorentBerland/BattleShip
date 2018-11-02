name := "Battleship"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.13"

// Testing
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

mainClass in assembly := Some("HumanVsChosenOpponent")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
assemblyOutputPath in assembly := file(s"./${name.value}-${version.value}.jar")