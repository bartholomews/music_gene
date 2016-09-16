package model.constraints

import model.music.Attribute

/**
  * Potential information about a `Score` result
  *
  * @param attr the Attribute which is tested on that Score
  * @param index the index of the song tested
  * @param distance the distance result of the evaluation, default at 0.0
  *                 for constraints which do not reason in terms of distance
  */
case class Info(attr: Attribute, index: Int, distance: Double = 0.0)
