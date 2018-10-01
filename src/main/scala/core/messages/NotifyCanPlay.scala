package core.messages

import akka.actor.ActorRef
import core.model.ShotGrid


/**
  * Notify the player that he can now play
  *
  * @param nextActor The actor to send back an answer
  * @param shotGrid The play grid the player should use
  */
class NotifyCanPlay(val nextActor: ActorRef, val shotGrid: ShotGrid)
