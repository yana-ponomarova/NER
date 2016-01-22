package main.scala.ner

import edu.stanford.nlp.ling.CoreAnnotations.{NamedEntityTagAnnotation, TokensAnnotation, SentencesAnnotation}
import edu.stanford.nlp.pipeline._
import java.util
import java.util.Properties
import javax.swing.JOptionPane

import edu.stanford.nlp.ie.NERClassifierCombiner

import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.{CoreLabel, CoreAnnotations}
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern

//remove if not needed
import scala.collection.JavaConversions._

/*
Stanford NER Demo file rewritten in Scala to deal with multiple classifiers
 */

object testRegexNer {






  def main(args: Array[String]) {




    val REGEX_ANNOTATOR_NAME = "tokensregexner"

    val MAPPING = "classifiers/classeur1.txt"



    val props = new Properties()

    props.setProperty(REGEX_ANNOTATOR_NAME + ".mapping", MAPPING)
    props.setProperty(REGEX_ANNOTATOR_NAME + ".ignorecase", "true")
    props.setProperty(REGEX_ANNOTATOR_NAME + ".backgroundSymbol","O, LOCATION")
    val regexAnnotator: Annotator = new TokensRegexNERAnnotator(REGEX_ANNOTATOR_NAME, props)


    val properties = new Properties()


    properties.setProperty("annotators", "tokenize, ssplit, pos, lemma")
    val corenlp = new StanfordCoreNLP(properties)





    val serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz"
    var serializedClassifier1 = "classifiers/CrudeSupply2.classifier.ser.gz"
    if (args.length > 0) {
      serializedClassifier1 = args(0)
    }

    val classifier = new NERClassifierCombiner(false, false, serializedClassifier1, serializedClassifier)



    val nerAnnotator = new NERCombinerAnnotator(false,  serializedClassifier1, serializedClassifier)




    if (args.length > 1) {
      val fileContents = IOUtils.slurpFile(args(1))
      // making the result with NERClassifierCombiner
      var out = classifier.classify(fileContents)
      out = classifier.classifyFile(args(1))

      // making the result with NERCombinerAnnotator
      val document1 = new Annotation(fileContents)

      corenlp.annotate(document1)

      nerAnnotator.annotate(document1)




      println("--- resultat avec out-------")



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

      regexAnnotator.annotate(document1)
      println("--- resultat avec combiner Annotator-------")

      var sentences = document1.get(classOf[SentencesAnnotation])
      for (sentence <- sentences) {
        for (token <- sentence.get(classOf[TokensAnnotation])) {
          System.out.print(token.word() + "/" + token.get(classOf[NamedEntityTagAnnotation]) + ' ')
        }
        println()
      }

      println("--- resultat après tokenRegex-------")

      sentences = document1.get(classOf[SentencesAnnotation])
      for (sentence <- sentences) {
        for (token <- sentence.get(classOf[TokensAnnotation])) {
          System.out.print(token.word() + "/" + token.get(classOf[NamedEntityTagAnnotation]) + ' ')
        }
        println()
      }




    }
  }
}

