package core.messages

import akka.actor.ActorRef
import core.model.ShotGrid


/**
  * Notify the player that he can now play
  *
  * @param nextActor The actor to send back an answer
  * @param shotGrid The play grid the player should use
  * @param playerId The id of the player
  */
class NotifyCanPlay(val nextActor: ActorRef, val shotGrid: ShotGrid, val playerId: String)
