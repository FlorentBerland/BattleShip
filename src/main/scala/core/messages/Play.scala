package core.messages

import akka.actor.ActorRef
import java.awt.Point


/**
  * This message should be sent to the engine to play a round
  *
  * @param playerUid The identifier of the message sender
  * @param shotCoordinates The coordinates of the shot
  */
class Play(val playerUid: String, val shotCoordinates: Point)
