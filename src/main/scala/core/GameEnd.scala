package core

object GameEnd extends Enumeration {
  type End = Value
  val VICTORY, DEFEAT, STOPPED = Value
}
