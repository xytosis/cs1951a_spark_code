import org.apache.spark.{SparkConf, SparkContext}
import spark.jobserver.{SparkJob, SparkJobValid, SparkJobValidation}
import com.typesafe.config.{Config, ConfigFactory}

import scalaj.http.Http

object TestJob extends SparkJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[4]").setAppName("Solr Application")
    val sc = new SparkContext(conf)
    val config = ConfigFactory.parseString("")
    runJob(sc, config)
  }

  override def runJob(sc:SparkContext, jobConfig: Config): Any = {
    val response = Http("http://54.173.242.173:8983/solr/comments/select")
      .param("q","body:stupid")
      .param("rows","1")
      .param("fl","body")
      .param("wt","json")
      .asString.body
    val lines = response.split("\n")
    val logData = sc.parallelize(lines)
    logData.flatMap(line => line.split(" ")).countByValue
  }

  override def validate(sc:SparkContext, config: Config): SparkJobValidation = SparkJobValid
}