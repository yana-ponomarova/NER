package main.scala.ner

import edu.stanford.nlp.ie.crf._
import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.CoreAnnotations
//remove if not needed
import scala.collection.JavaConversions._

 /*
 Stanford NER Demo file rewritten in Scala
  */

object NERDemo {

  def main(args: Array[String]) {
    var serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz"
    if (args.length > 0) {
      serializedClassifier = args(0)
    }
    val classifier = CRFClassifier.getClassifier(serializedClassifier)
    if (args.length > 1) {
      val fileContents = IOUtils.slurpFile(args(1))
      var out = classifier.classify(fileContents)
      println("---")
      out = classifier.classifyFile(args(1))
      for (sentence <- out) {
        for (word <- sentence) {
          System.out.print(word.word() + '/' +
            word.get(classOf[CoreAnnotations.AnswerAnnotation]) +
            ' ')
        }
        println()
      }
      println("---")
      val list = classifier.classifyToCharacterOffsets(fileContents)
      for (item <- list) {
        println(item.first() + ": " +
          fileContents.substring(item.second(), item.third()))
      }
    } else {
      val example = Array("Good afternoon Rajat Raina, how are you today?", "The refinery will be able to process any mixture of Basrah Regular Crude Oil (34ºAPI) and Basrah Mishrif Crude Oil (28ºAPI)")
      for (str <- example) {
        println(classifier.classifyToString(str))
      }
      println("---")
      for (str <- example) {
        System.out.print(classifier.classifyToString(str, "slashTags", false))
      }
      println("---")
      for (str <- example) {
        System.out.print(classifier.classifyToString(str, "tabbedEntities", false))
      }
      println("---")
      for (str <- example) {
        println(classifier.classifyWithInlineXML(str))
      }
      println("---")
      for (str <- example) {
        println(classifier.classifyToString(str, "xml", true))
      }
      println("---")
      for (str <- example) {
        System.out.print(classifier.classifyToString(str, "tsv", false))
      }


    }
  }
}

