import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.net.URLEncoder

import scala.io.Source

object SolrApp {
  def main(args: Array[String]) {
    val url_stem = "http://52.207.213.209:8983/solr/comments/select?q=";

    String q = "random word £500 bank $";
    String url = "http://example.com/query?q=" + URLEncoder.encode(q, "UTF-8");
    println(url);
    
    val response = Source.fromURL(url_stem + "body%3Astupid&rows=1&fl=body").mkString
    val conf = new SparkConf().setAppName("Solr Application")
    val sc = new SparkContext(conf)
    val lines = response.split("\n")
    val logData = sc.parallelize(lines)
    val counts = logData.flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
      .collect()
    counts.foreach{
      word =>
        println("word: " + word._1 + ", " + "count: " + word._2)
    }
  }
}
