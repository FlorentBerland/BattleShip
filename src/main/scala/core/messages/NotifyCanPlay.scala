package core.messages

import akka.actor.ActorRef


/**
  * Notify the player that he can now play
  *
  * @param sender The sender of the message
  */
class NotifyCanPlay(val sender: ActorRef)
