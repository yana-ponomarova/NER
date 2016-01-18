import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
  * Created by nico on 28/12/2015.
  */
object RelationExtraction {

  def getTrees(s: String, pipeline: StanfordCoreNLP) = {
    val document = new Annotation(s)
    pipeline.annotate(document)
    val sentences = document.get(classOf[SentencesAnnotation])
    var sentenceTrees = List[Tree]()
    sentences.foreach(sentenceTrees::= _.get(classOf[TreeAnnotation]))
    sentenceTrees
  }

  def partitionGetTrees(iter: Iterator[String]): Iterator[List[Tree]] = {
    // CoreNLP Initialisation
    val properties = new Properties()
    // annotator parse needs ssplit and tokenize
    properties.setProperty("annotators", "tokenize, ssplit, parse")

    // not sure if transient lazy is better than mapPartition
    // val pipeline = new SparkCoreNLP(properties).get
    iter.map(line => getTrees(line, new StanfordCoreNLP(properties)))
  }

  def main(args: Array[String]) {
    /*    if (args.length < 2) {
          System.err.println("Please set arguments for <s3_input_dir> <s3_output_dir>")
          System.exit(1)
        }
        val inputDir = args(0)
        val outputDir = args(1)*/
    val inputDir = "src/main/resources/sentences.txt"
    val conf = new SparkConf().setAppName("Entity Extraction").setMaster("local")
    val sc = new SparkContext(conf)
    val textFile = sc.textFile(inputDir)

    textFile.mapPartitions(lines => partitionGetTrees(lines)).foreach(println) // Rdd[Tree]

    sc.stop()
  }
}
