package blog.nebulagraph

import org.apache.spark.sql.SparkSession

@main def runSparkWithNebulaGraph(): Unit =
  val spark = SparkSession.builder
    .appName("NebulaGraph")
    .master("local[*]")
    .getOrCreate()
  import spark.implicits.*

  val df = List("hello", "world").toDF
  df.show()

  spark.stop
