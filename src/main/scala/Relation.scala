import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
import edu.stanford.nlp.trees.GrammaticalRelation
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations

import scala.collection.JavaConversions._


/**
  * Created by yana on 07/01/2016.
  */
class Relation {

  private val set_modifiers = Set(UniversalEnglishGrammaticalRelations.ADJECTIVAL_MODIFIER,
    UniversalEnglishGrammaticalRelations.COMPOUND_MODIFIER,
    UniversalEnglishGrammaticalRelations.NUMERIC_MODIFIER,
    UniversalEnglishGrammaticalRelations.getNmod("of"))

  private val location_modifiers_set = Set(UniversalEnglishGrammaticalRelations.getNmod("in"),
    UniversalEnglishGrammaticalRelations.getNmod("at"), UniversalEnglishGrammaticalRelations.getNmod("from"))
  /**
    * @param semGraph
    * @return True if the semGraph is passive
    */
  def isActive(semGraph: SemanticGraph) : Boolean = {
    semGraph.findAllRelns(UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT).length > 0
  }

  /**
    * @param semGraph
    * @return True if the semGraph is passive
    */
  def isPassive(semGraph: SemanticGraph) : Boolean = {
    semGraph.findAllRelns(UniversalEnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT).length > 0
  }

  /**
    *
    * @param semanticGraph
    * @param index could be a supplier, a theme, a receiver
    * @return
    */
  def getStrFromIndex(semanticGraph: SemanticGraph, index: IndexedWord) : String = {
    //TODO Should use option or Some
    if (index != null) {
      val indexModifiers = semanticGraph.getChildrenWithRelns(index, set_modifiers)
      val indexFull = indexModifiers + index
      val indexFullSorted = indexFull.toList.sortWith(_.index() < _.index())
      indexFullSorted.map(f => f.word()).mkString(" ")
    }
    else {
      "" // should disapear with the null
    }
  }

  def  getSupplierNmod(semGraph: SemanticGraph, supplier: IndexedWord) : String = {
    if (supplier != null) {
      val supplierModifiers = semGraph.getChildrenWithRelns(supplier, set_modifiers)

      val supplierNmodLocation = semGraph.getChildrenWithRelns(supplier, location_modifiers_set)
      val supplierNmodLocationModifiers = supplierNmodLocation.flatMap(f => semGraph.getChildrenWithRelns(f, set_modifiers + UniversalEnglishGrammaticalRelations.CASE_MARKER))

      val supplierFull = supplierModifiers + supplier ++ supplierNmodLocation ++ supplierNmodLocationModifiers
      val supplierFullSorted = supplierFull.toList.sortWith(_.index() < _.index())
      supplierFullSorted.map(f => f.word()).mkString(" ")
    }
    else {
      ""
    }
  }

  def getTupleRelation(root : IndexedWord, supplier: String, receiver: String, theme: String) : (String, String, String, String) = {
    (root.lemma(), supplier, receiver, theme)
  }

  /**
    * Determine the voice of the sentence :
    * If the sentence is in active voice, a 'nsubj' dependency should exist.
    * If the sentence is in passive voice a 'nsubjpass' dependency should exist
    *
    * @param semGraph
    * @return
    */
  def relationParsing(semGraph: SemanticGraph): (String, String, String, String) = {

    val active = isActive(semGraph)
    val passive = isPassive(semGraph)

    var res: (String, String, String, String) = ("", "", "", "")
    val root = semGraph.getFirstRoot()

    val outgoingVerbsSet = Set("pipe", "supply", "export", "send", "provide", "render", "distribute", "sell", "ply",
      "deliver", "transport", "transfer", "transmit", "channel", "send")
    val incomingVerbsSet = Set("receive", "get", "obtain", "incur ", "acquire", "buy", "purchase", "charter", "take",
      "bring", "source", "gather", "collect", "import", "extract", "derive", "procure")
    val ambiguousVerbsSet = Set("ship")
    val arrivalVerbsSet = Set("come", "arrive", "get")
    val usageVerbsSet = Set("use", "consume", "enjoy", "benefit", "employ", "apply", "exploit", "tap", "utilize", "")

    var supplierFullString: String = ""
    var receiverFullString: String = ""
    var themeFullString: String = ""


    val copula = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.COPULA)
    //TODO Replace all the null testing
    if (copula != null) {
      val theme = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT)
      val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("from"))

      themeFullString = getStrFromIndex(semGraph, theme)
      supplierFullString = getStrFromIndex(semGraph, supplier)
      res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
    }

    // Active Voice
    else if (active) {

      if (outgoingVerbsSet.contains(root.lemma())) {
        var theme : IndexedWord = null
        val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT)
        val obj = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.DIRECT_OBJECT)

        if (obj != null) {
          val objNmod = semGraph.getChildWithReln(obj, UniversalEnglishGrammaticalRelations.getNmod("with"))

          if (objNmod == null) {
            theme = obj
            themeFullString = getStrFromIndex(semGraph, theme)
            val receiverModifiersSet = Set(UniversalEnglishGrammaticalRelations.getNmod("to"), UniversalEnglishGrammaticalRelations.getNmod("for"))
            val themeReceivers = semGraph.getChildrenWithRelns(theme, receiverModifiersSet)
            if (themeReceivers.size() > 0) {
              val themeReceiversModifiers = themeReceivers.flatMap(f => semGraph.getChildrenWithRelns(f, set_modifiers))
              val receiversFull = themeReceivers ++ themeReceiversModifiers
              val receiversFullSorted = receiversFull.toList.sortWith(_.index() < _.index())
              receiverFullString = receiversFullSorted.map(f => f.word()).mkString(" ")
            }
          }
          else {
            val themeReceiver = obj
            theme = objNmod
            receiverFullString = getStrFromIndex(semGraph, themeReceiver)
          }
        }
        supplierFullString = getStrFromIndex(semGraph, supplier)
        themeFullString = getStrFromIndex(semGraph, theme)
        res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
      }

      // should it be else if ? otherwise res might be rewritten
      if ((incomingVerbsSet.contains(root.lemma())) | usageVerbsSet.contains(root.lemma())) {
        val receiver = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT)
        val theme = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.DIRECT_OBJECT)
        val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("from"))

        receiverFullString = getStrFromIndex(semGraph, receiver)
        themeFullString = getStrFromIndex(semGraph, theme)
        supplierFullString = getSupplierNmod(semGraph, supplier)
        res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
      }

      // Could this be a test with copula ?
      if (arrivalVerbsSet.contains(root.lemma())) {
        val theme = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT)
        val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("from"))

        themeFullString = getStrFromIndex(semGraph, theme)
        supplierFullString = getSupplierNmod(semGraph, supplier)
        res = getTupleRelation(root, supplierFullString, "", themeFullString)
      }
    }

    // Passive voice
    else if (passive) {
      if (outgoingVerbsSet.contains(root.lemma())) {
        val root = semGraph.getFirstRoot()
        val children = semGraph.childRelns(root)
        val theme = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT)
        val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("agent"))
        val receiver = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("to"))

        themeFullString = getStrFromIndex(semGraph, theme)
        supplierFullString = getStrFromIndex(semGraph, supplier)
        receiverFullString = getStrFromIndex(semGraph, receiver)
        res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
      }

      // should it be else if ? otherwise res might be rewritten
      if (incomingVerbsSet.contains(root.lemma())) {
        val root = semGraph.getFirstRoot()
        val children = semGraph.childRelns(root)
        val theme = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT)
        val supplier = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("from"))
        val receiver = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.getNmod("agent"))

        themeFullString = getStrFromIndex(semGraph, theme)
        supplierFullString = getStrFromIndex(semGraph, supplier)
        receiverFullString = getStrFromIndex(semGraph, receiver)
        res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
      }
    }

    // Undetermined
    else if ((!active).&&(!passive)) {

      if (ambiguousVerbsSet.contains(root.lemma())) {
        val nom_subj = semGraph.getChildWithReln(root, UniversalEnglishGrammaticalRelations.COMPOUND_MODIFIER)
        val theme = semGraph.getChildWithReln(root, GrammaticalRelation.DEPENDENT)
        val theme_from = semGraph.getChildWithReln(theme, UniversalEnglishGrammaticalRelations.getNmod("from"))
        val theme_to = semGraph.getChildWithReln(theme, UniversalEnglishGrammaticalRelations.getNmod("to"))

        if (theme != null) {
          if (theme_from != null) {
            val supplier = theme_from
            val receiver = nom_subj

            supplierFullString = getSupplierNmod(semGraph, supplier)
            receiverFullString = getStrFromIndex(semGraph, receiver)
            themeFullString = getStrFromIndex(semGraph, theme)
          }
            // WARNING THIS IS OVERRIDE PREVIOUS
          else if (theme_to != null) {
            val supplier = nom_subj
            val receiver = theme_to

            supplierFullString = getSupplierNmod(semGraph, supplier)
            receiverFullString = getStrFromIndex(semGraph, receiver)
            themeFullString = getStrFromIndex(semGraph, theme)
          }
        }
        res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
      }
    }
    // IF THiS are only else if we can do:
    // res = getTupleRelation(root, supplierFullString, receiverFullString, themeFullString)
    res
  }

  def getRelationships(s: String, pipeline: StanfordCoreNLP) : String = {

    val document = new Annotation(s)
    pipeline.annotate(document)
    val sentences = document.get(classOf[SentencesAnnotation])
    var CollapsedSentenceDep = List[SemanticGraph]()
    sentences.foreach(CollapsedSentenceDep ::= _.get(classOf[CollapsedCCProcessedDependenciesAnnotation]))
    var Relations = List[(String, String, String, String)]()
    Relations = CollapsedSentenceDep.map(sg => relationParsing(sg))
    val CollapsedSentenceDepString = CollapsedSentenceDep.toList.map(sg => sg.toString)
    val pair = sentences zip CollapsedSentenceDepString zip Relations
    pair.mkString("=>")

    //var SentenceTriples = List[Array[RelationTriple]]()
    //sentences.foreach(SentenceTriples ::= _.get(classOf[RelationTriplesAnnotation]))
    //val SentenceTriples = sentences.get(0).get(classOf[RelationTriplesAnnotation])

    /*
  Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
  // Print the triples
  for (RelationTriple triple : triples) {
    System.out.println(triple.confidence + "\t" +
      triple.subjectLemmaGloss() + "\t" +
      triple.relationLemmaGloss() + "\t" +
      triple.objectLemmaGloss());
  }
  */
  }
}
