import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import scala.io.Source

object SolrApp {
  def main(args: Array[String]) {
    val response = Source.fromURL("http://52.207.213.209:8983/solr/comments/select?q=body%3Astupid&rows=1&fl=body").mkString
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
