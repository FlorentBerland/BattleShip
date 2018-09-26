package model.battle

object ShotResult extends Enumeration {
  type Result = Value
  val MISS, HIT, HIT_AND_SINK = Value
}
