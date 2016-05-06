import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.native.JsonMethods._
import spark.jobserver.{SparkJob, SparkJobValid, SparkJobValidation}

import scalaj.http.Http

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

object SentimentAnalyzerJob extends SparkJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[4]").setAppName("Sentiment Analyzer")
    val sc = new SparkContext(conf)
    val config = ConfigFactory.parseString("")
    runJob(sc, config)
  }

  override def validate(sc:SparkContext, config: Config): SparkJobValidation = SparkJobValid

  override def runJob(sc:SparkContext, jobConfig: Config): Any = {
    val pipeline = initPipeline()
    val comments = getComments()
    val logData = sc.parallelize(comments)
    logData
      .flatMap(line => line.body.split("."))
      .map(sentence => analyzeComment(sentence, pipeline))
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
      .param("q","body:stupid")
      .param("rows","10")
      .param("fl","body")
      .param("wt","json")
      .asString.body
    val jvalue = parse(json) \ "response" \ "docs"
    jvalue.extract[List[Body]]
  }

  def analyzeComment(comment: String, pipeline: StanfordCoreNLP): Double = {
    var sumSentiment : Double = 0.0

    val annotation : Annotation = pipeline.process(comment)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    val moo = sentences.
      .map(sentence => sentence.get(classOf[SentimentCoreAnnotations.AnnotatedTree]))
      .map { case (tree) => RNNCoreAnnotations.getPredictedClass(tree) }
      .toList

    moo.foreach(sumSentiment += _)
    return sumSentiment/moo.length
  }
}