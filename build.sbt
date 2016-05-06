name := "SimpleProject"

version := "1.0"

scalaVersion := "2.11.5"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
"edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")))

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"