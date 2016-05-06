import org.apache.spark.{SparkConf, SparkContext}
import spark.jobserver.{SparkJob, SparkJobValid, SparkJobValidation}
import com.typesafe.config.{Config, ConfigFactory}

import scalaj.http.Http
import org.json4s._
import org.json4s.native.JsonMethods._

object TestJob extends SparkJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[4]").setAppName("Solr Application")
    val sc = new SparkContext(conf)
    val config = ConfigFactory.parseString("")
    runJob(sc, config)
  }

  override def validate(sc:SparkContext, config: Config): SparkJobValidation = SparkJobValid

  override def runJob(sc:SparkContext, jobConfig: Config): Any = {
    val jsonResponse = Http("http://54.173.242.173:8983/solr/comments/select")
      .param("q","body:stupid")
      .param("rows","10")
      .param("fl","body")
      .param("wt","json")
      .asString.body
    val comments = getComments(jsonResponse)
    val logData = sc.parallelize(comments)
    logData.flatMap(line => line.split(" ")).countByValue
  }

  def getComments(json: String): List[String] = {
    case class Body(body: String)
    implicit val formats = DefaultFormats
    val jvalue = parse(json) \ "response" \ "docs"
    jvalue.extract[List[Body]].map(b => b.body)
  }
}