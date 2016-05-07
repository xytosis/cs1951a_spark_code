import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.native.JsonMethods._
import spark.jobserver.{SparkJob, SparkJobValid, SparkJobValidation}

import scalaj.http.{Http, HttpOptions}
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.convert.wrapAll._

object SentimentAnalyzerJob extends SparkJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Sentiment Analyzer")
    val sc = new SparkContext(conf)
    val config = ConfigFactory.parseString("")
    runJob(sc, config)
  }

  override def validate(sc:SparkContext, config: Config): SparkJobValidation = SparkJobValid

  override def runJob(sc:SparkContext, jobConfig: Config): Any = {
    val comments = getComments()
    val logData = sc.parallelize(comments)
    logData
      .map(line => line.body.split('.'))
      .map(sentence =>
        analyzeComment(sentence))
      .filter(x => !x.isNaN)
      .mean()
  }

  def initPipeline(): StanfordCoreNLP = {
    val props : Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
    new StanfordCoreNLP(props)
  }

  case class Body(body: String)
  implicit val formats = DefaultFormats

  def getComments(): List[Body] = {
    val json = Http("http://54.173.242.173:8983/solr/comments/select")
      .param("q","body:reddit")
      .param("rows","100")
      .param("fl","body")
      .param("wt","json")
      .param("fq", "votescore:[2 TO *]")
      //      .param("sort","votescore%20desc")
      .option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(50000))
      .asString.body
    val jvalue = parse(json) \ "response" \ "docs"
    jvalue.extract[List[Body]]
  }

  def analyzeComment(comments: Array[String]): Double = {
    val pipeline = initPipeline()
    var sumAll : Double = 0
    for (comment <- comments) {
      var sumSentiment: Double = 0
      val annotation : Annotation = pipeline.process(comment)
      val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
      val moo = sentences
        .map(sentence => sentence.get(classOf[SentimentCoreAnnotations.AnnotatedTree]))
        .map { case (tree) => RNNCoreAnnotations.getPredictedClass(tree) }
        .toList
      moo.foreach(sumSentiment += _)

      sumAll += sumSentiment/moo.length
    }

    return sumAll/comments.length
  }
}