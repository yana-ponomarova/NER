package main.scala.ner

import main.scala.classes.CrfTrainer

/**
  * Created by Selsabil on 14/01/16.
  */
object Train {
  def main(args: Array[String]) {
    //CrfTrainer.trainClassifier("FileName","SerializeTo")
    //CrfTrainer.trainClassifier("classifiers/CrudeSupply2.tsv","classifiers/CrudeSupply2.classifier.ser.gz" ,"src/main/ressources/properties.prop")
    CrfTrainer.trainClassifier("classifiers/CrudeSupply_y.tsv","classifiers/CrudeSupply2.classifier.ser.gz", gazLocation = "src/main/ressources/gazetteer.txt", auxClissifierLocation = "classifiers/english.muc.7class.distsim.crf.ser.gz")
  }


}
