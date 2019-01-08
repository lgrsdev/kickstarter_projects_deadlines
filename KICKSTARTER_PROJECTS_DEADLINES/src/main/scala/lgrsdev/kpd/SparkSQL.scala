package lgrsdev.kpd

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._
import org.apache.spark.sql.functions.{ to_date, col }
import scala.tools.scalap.Main

object SparkSQL {
    
def main( args:Array[String] ) {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("KICKSTARTER_PROJECTS_DEADLINES")
      .master("local[*]")
      .getOrCreate()

    val df = spark.sqlContext.read
      .option("header", true)
      .option("delimiter", ",")
      //    .option("multiLine", true)
      .csv("ks-projects-201801.csv")

    val deadlines = df.select("deadline")
    deadlines.printSchema

    import spark.implicits._
    val modifiedDeadlines = deadlines.withColumn("deadline", to_date($"deadline", "yyyy-MM-dd"))
    modifiedDeadlines.printSchema

    val url = "jdbc:mysql://localhost:3306/kickstarter"
    val table = "deadlines"

    //write data from spark dataframe to database
    modifiedDeadlines.write.format("jdbc").mode("append")
      .option("url", url)
      .option("dbtable", table)
      .option("user", "root")
      .option("password", "root")
      .option("numPartitions", "100")
      .save()

    spark.stop()
  }
}