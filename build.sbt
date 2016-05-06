name := "SimpleProject"

version := "1.0"

scalaVersion := "2.10.5"

resolvers ++= Seq(
  "Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven",
  "Akka Repository" at "http://repo.akka.io/releases/")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "spark.jobserver" %% "job-server-api" % "0.6.2" % "provided")
