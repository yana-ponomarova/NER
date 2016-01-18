import java.util.Properties

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.semgraph.SemanticGraph
import org.scalatest.FlatSpec

/**
  * Created by nico on 17/01/2016.
  */

class RelationSpec extends FlatSpec {

  val relation = new Relation()
  val properties = new Properties()
  properties.setProperty("annotators", "tokenize, ssplit, parse, lemma")
  val corenlp = new StanfordCoreNLP(properties)

  //println(relation.getRelationships(refinery, corenlp))

//  "A passive graph" should "be true with isPassive " {
//    assert(true == relation.isPassive(new SemanticGraph()))
//  }

  "Yana should explain getRelationships to nico" should "be the following sentence" in {
    val gdf = "Gaz de France provides the site with its fuel gas requirements."
    val gdfRln = "((Gaz de France provides the site with its fuel gas requirements.,-> provides/VBZ (root)\n  -> France/NNP (nsubj)\n    -> Gaz/NNP (compound)\n    -> de/FW (compound)\n  -> site/NN (dobj)\n    -> the/DT (det)\n    -> requirements/NNS (nmod:with)\n      -> with/IN (case)\n      -> its/PRP$ (nmod:poss)\n      -> fuel/NN (compound)\n      -> gas/NN (compound)\n),(provide,Gaz de France,site,fuel gas requirements))"

    assert(gdfRln == relation.getRelationships(gdf, corenlp))
  }

  "Yana should explain getRelationships to nico pourcent" should "be the following sentence" in {
    val pourcent = "Gaz de France provides around 30-40% of the fuel gas requirements for the site."
    val pourcentRln = "((Gaz de France provides around 30-40% of the fuel gas requirements for the site.,-> provides/VBZ (root)\n  -> France/NNP (nsubj)\n    -> Gaz/NNP (compound)\n    -> de/FW (compound)\n  -> %/NN (nmod:around)\n    -> around/IN (case)\n    -> 30-40/CD (nummod)\n    -> requirements/NNS (nmod:of)\n      -> of/IN (case)\n      -> the/DT (det)\n      -> fuel/NN (compound)\n      -> gas/NN (compound)\n      -> site/NN (nmod:for)\n        -> for/IN (case)\n        -> the/DT (det)\n),(provide,Gaz de France,,))"

    assert(pourcentRln == relation.getRelationships(pourcent, corenlp))
  }

  "Yana should explain getRelationships to nico crude" should "be the following sentence" in {
    val crude = "Crude Oil is available from the Kumkol Oil Field."
    val crudeRln = "((Crude Oil is available from the Kumkol Oil Field.,-> available/JJ (root)\n  -> Oil/NNP (nsubj)\n    -> Crude/NNP (compound)\n  -> is/VBZ (cop)\n  -> Field/NNP (nmod:from)\n    -> from/IN (case)\n    -> the/DT (det)\n    -> Kumkol/NNP (compound)\n    -> Oil/NNP (compound)\n),(available,Kumkol Oil Field,,Crude Oil))"

    assert(crudeRln == relation.getRelationships(crude, corenlp))
  }

  "Yana should explain getRelationships to nico refinery" should "be the following sentence" in {
    val refinery = "The refinery processes local Hassi Messaoud Crude Oil, which is supplied by pipeline."
    val refineryRln = "((The refinery processes local Hassi Messaoud Crude Oil, which is supplied by pipeline.,-> processes/VBZ (root)\n  -> refinery/NN (nsubj)\n    -> The/DT (det)\n  -> Oil/NNP (dobj)\n    -> local/JJ (amod)\n    -> Hassi/NNP (compound)\n    -> Messaoud/NNP (compound)\n    -> Crude/NNP (compound)\n    -> supplied/VBN (acl:relcl)\n      -> which/WDT (nsubjpass)\n      -> is/VBZ (auxpass)\n      -> pipeline/NN (nmod:agent)\n        -> by/IN (case)\n),(,,,))"

    assert(refineryRln == relation.getRelationships(refinery, corenlp))
  }

}
