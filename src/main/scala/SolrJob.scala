import org.apache.spark.{SparkConf, SparkContext}
import spark.jobserver.{SparkJob, SparkJobValid, SparkJobValidation}
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.Source

/**
  * Created by Anthony on 5/5/2016.
  */
object SolrJob extends SparkJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[4]").setAppName("Solr Application")
    val sc = new SparkContext(conf)
    val config = ConfigFactory.parseString("")
    runJob(sc, config)
  }

  override def runJob(sc:SparkContext, jobConfig: Config): Any = {
    val response = Source.fromURL("http://54.173.242.173:8983/solr/comments/select?q=body%3Astupid&rows=1&fl=body&wt=json").mkString
    val lines = response.split("\n")
    val logData = sc.parallelize(lines)
    logData.flatMap(line => line.split(" ")).countByValue
//    counts.foreach{
//      word =>
//        println("word: " + word._1 + ", " + "count: " + word._2)
//    }
  }

  override def validate(sc:SparkContext, config: Config): SparkJobValidation = SparkJobValid
}
