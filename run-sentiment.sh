~/spark/bin/spark-submit \
    --class "SentimentAnalysisApp" \
    --master local[4] \
    target/scala-2.11/SimpleProject-assembly-1.0.jar
