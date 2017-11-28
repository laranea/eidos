package org.clulab.wm.entities

import com.typesafe.scalalogging.LazyLogging
import org.clulab.openie.entities.RuleBasedEntityFinder
import org.clulab.odin.{ExtractorEngine, Mention, State}
import org.clulab.openie.ResourceUtils
import org.clulab.processors.Document

class AgroEntityFinder(
  entityEngine: ExtractorEngine,
  avoidEngine: ExtractorEngine,
  maxHops: Int
 ) extends RuleBasedEntityFinder(entityEngine: ExtractorEngine, avoidEngine: ExtractorEngine, maxHops: Int) {

  /**
    * Task-specific implementation of extract to find entities in documents.  Additions include (TODO) handling of
    * domain Params, and (TODO) converting to Mentions with Modifications
    * @param doc: Document
    * @return Seq[Mention] of entities
    */
  override def extract(doc: Document): Seq[Mention] = {
    // avoid refs, etc.
    val avoid = avoidEngine.extractFrom(doc)
    val stateFromAvoid = State(avoid)
    val baseEntities = entityEngine.extractFrom(doc, stateFromAvoid).filter{ entity => ! stateFromAvoid.contains(entity) }
    //  logger.debug(s"Before expansion:\n ${entities.sortBy(_.start).foreach(e => println(s"${e.label} (${e.foundBy}): '${e.text} (${e.start}, ${e.end})"))}")
    val expandedEntities: Seq[Mention] = baseEntities.map(entity => expand(entity, maxHops))
    // split entities on likely coordinations
    val splitEntities = (baseEntities ++ expandedEntities).flatMap(splitCoordinatedEntities)
    // remove entity duplicates introduced by splitting expanded
    val distinctEntities = splitEntities.distinct
    // if there are no avoid mentions, no need to filter
    val res = if (avoid.isEmpty) {
      distinctEntities
    } else {
      // check that our expanded entities haven't swallowed any avoid mentions
      val avoidLabel = avoid.head.labels.last
      distinctEntities.filter{ m => stateFromAvoid.mentionsFor(m.sentence, m.tokenInterval, avoidLabel).isEmpty }
    }
    res
  }

}

object AgroEntityFinder extends LazyLogging {

  val DEFAULT_MAX_LENGTH = 10 // maximum length (in tokens) for an entity
  def apply(maxHops: Int, maxLength: Int = DEFAULT_MAX_LENGTH): RuleBasedEntityFinder = {
    val entityRules = ResourceUtils.readResource("org/clulab/openie/entities/grammar/entities.yml")
    val avoidRules = ResourceUtils.readResource("org/clulab/openie/entities/grammar/avoid.yml")

    val avoidEngine = ExtractorEngine(avoidRules)
    val entityEngine = ExtractorEngine(entityRules)
    new RuleBasedEntityFinder(avoidEngine = avoidEngine, entityEngine = entityEngine, maxHops = maxHops)
  }


}