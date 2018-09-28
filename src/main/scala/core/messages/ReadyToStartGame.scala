package core.messages

import akka.actor.ActorRef

/**
  * Inform the player that the game will begin
  *
  * @param nextActor The next actor to respond
  */
class ReadyToStartGame(val nextActor: ActorRef)
