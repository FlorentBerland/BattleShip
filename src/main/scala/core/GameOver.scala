package core

object GameOver extends Enumeration {
  type End = Value
  val VICTORY, DEFEAT, STOPPED = Value
}
