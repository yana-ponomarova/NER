package main.scala.classes

import java.io.{StringReader, BufferedReader}
import scala.collection.JavaConversions._
import edu.stanford.nlp.process.PTBTokenizer


object Tokenizer {

  /*
    Just the Stanford PTB tokenizer.  Returns indexedSeq of tokens.
   */
    def tokenize(text: String): IndexedSeq[String] = {

      val tokenizer = PTBTokenizer.newPTBTokenizer(new BufferedReader(new StringReader(text)))

      tokenizer
        .tokenize()
        .map(_.word())
        .toIndexedSeq
    }

}
