package core.messages

import akka.actor.ActorRef
import java.awt.Point


/**
  * This message should be sent to the engine to play a round
  *
  * @param sender The sender of the message
  * @param shotCoordinates The coordinates of the shot
  */
class Play(val sender: ActorRef, val shotCoordinates: Point)
