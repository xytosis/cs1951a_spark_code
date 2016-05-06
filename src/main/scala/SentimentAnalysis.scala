import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.net.URLEncoder

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import edu.stanford.nlp._

import scala.collection.convert.wrapAll._

import scala.io.Source

object SentimentAnalysisApp {
  def analyzeComment(comment : String, pipeline :StanfordCoreNLP): Double = {
    var sumSentiment = 0.0;

    val annotation : Annotation = pipeline.process(comment)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    val moo = sentences
      .map(sentence => sentence.get(classOf[SentimentCoreAnnotations.AnnotatedTree]))
      .map { case (tree) => RNNCoreAnnotations.getPredictedClass(tree) }
      .toList

    moo.foreach(sumSentiment += _)
    return sumSentiment/moo.length
  }
  def main(args: Array[String]) {
    val word = "obama"
    val url_stem = "http://54.173.242.173:8983/solr/comments/select?q=body%3A";
    val limit = 10
//    http://54.173.242.173:8983/solr/comments/select?q=body%3Afuck&rows=10&wt=json&fl=body
    val response = Source.fromURL(url_stem + word + "&rows=" + limit + "&wt=json&fl=body").mkString
    val props : Properties = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
    val pipeline : StanfordCoreNLP = new StanfordCoreNLP(props);

    val comments : Array[String] = Array("Obama's policies are quite strange.", "I am so happy Obama is my president")

    var sum = 0.0

    for (comment <- comments){
      sum += analyzeComment(comment, pipeline)
    }

    println(sum/comments.length)

    // val conf = new SparkConf().setAppName("Test Application")
    // val sc = new SparkContext(conf)
    // val lines = response.split("\n")
    // val logData = sc.parallelize(lines)
    // val counts = logData.flatMap(line => line.split(" "))
    //   .map(word => (word, 1))
    //   .reduceByKey(_ + _)
    //   .collect()
    // counts.foreach{
    //   word =>
    //     println("word: " + word._1 + ", " + "count: " + word._2)
    // }
  }
}