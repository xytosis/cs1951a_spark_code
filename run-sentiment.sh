~/spark/bin/spark-submit \
  --class SentimentAnalyzer \
  --master local[4] \
  target/scala-2.10/SimpleProject-assembly-1.0.jar
