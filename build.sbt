name := "SimpleProject"

version := "1.0"

scalaVersion := "2.10.5"

resolvers += "Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0",
  "spark.jobserver" %% "job-server-api" % "0.6.2" % "provided")