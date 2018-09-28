package core.messages

import akka.actor.ActorRef


/**
  * Sent by a player to choose its opponent
  *
  * @param nextActor The next actor to inform as this player (should be the same)
  * @param opponent The class name of the opponent chosen
  */
class UseGameConfig(val nextActor: ActorRef, val opponent: String)
