/* SimpleApp.scala */

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "file:///root/test.txt" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }
  
  def flesch_kincaid_function(str: String): Double = {
    var words = str.split("\\s+")
    val num_words = words.length
    var sentences = str.split("\\.+")
    val num_sentences = sentences.length
    var num_syllables = 0
    for (word <- words) num_syllables += findSyllables(word)
    val grade_level = 0.39*(num_words/num_sentences)+11.8*(num_syllables/num_words)-15.59
    grade_level
  }

  val determineVowel = (ch: Char) =>
    ("aeiouy" toList) exists (_ == ch)

  def findSyllables(str: String): Int = {
    var filteredWord = str.filter(Character.isLetter(_)).mkString
    var word = filteredWord.toLowerCase()

    if (filteredWord.length <= 3)
      return 1

    val wordMod = (filteredWord.toLowerCase()).toList
    val lastTwoLetters = (wordMod.takeRight(2)).mkString
    var totalNumberOfVowels = wordMod.count(determineVowel)

    // Subtract 1 from count if word ends in -es, -ed, or -e but not -le
    if (lastTwoLetters.equals("es") ||
      lastTwoLetters.equals("ed") ||
      (!lastTwoLetters.equals("le") &&
        !lastTwoLetters.equals("ee") &&
        (wordMod.last == 'e')))
      totalNumberOfVowels = totalNumberOfVowels-1

    // Subtract 1 if word begins with y
    if (wordMod.head == 'y')
      totalNumberOfVowels = totalNumberOfVowels-1

    var consSyllCount = 0
    for (i <- 0 until wordMod.length-1) {
      if (determineVowel(wordMod(i)) && determineVowel(wordMod(i+1))) {
        if (i == 0)
          totalNumberOfVowels = totalNumberOfVowels-1
        else if (!determineVowel(wordMod(i-1)))
          totalNumberOfVowels = totalNumberOfVowels-1
      }
    }
    totalNumberOfVowels
  }
}

/*
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import scalaj.http._

object SimpleApp {
  def main(args: Array[String]) {
    val response: HttpResponse[String] = Http("http://52.207.213.209:8983/solr/comments/select?q=body%3Astupid&rows=1&fl=body").asString
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val lines = response.body.split("\n")
    val logData = sc.parallelize(lines)
    val counts = logData.flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
    println(counts)
  }
}
*/
