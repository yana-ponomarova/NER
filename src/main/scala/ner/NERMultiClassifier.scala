package main.scala.ner

import edu.stanford.nlp.ie.NERClassifierCombiner

import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.CoreAnnotations
//remove if not needed
import scala.collection.JavaConversions._

/*
Stanford NER Demo file rewritten in Scala to deal with multiple classifiers
 */

object NERMultiClassifier {

  def main(args: Array[String]) {
    val serializedClassifier1 = "classifiers/english.muc.7class.distsim.crf.ser.gz"
    var serializedClassifier = "classifiers/CrudeSupply2.classifier.ser.gz"
    if (args.length > 0) {
      serializedClassifier = args(0)
    }

    val classifier = new NERClassifierCombiner(false, false, serializedClassifier, serializedClassifier1)

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
      val example = Array("Good afternoon Rajat Raina, how are you today?", "The refinery will be able to process any mixture of Basrah Regular Crude Oil (34ºAPI) and Basrah Mishrif Crude Oil (28ºAPI)", "I want to travel to Tunisia")
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

