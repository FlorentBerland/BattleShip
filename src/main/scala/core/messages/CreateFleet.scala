package core.messages

import java.awt.Dimension

import akka.actor.ActorRef
import core.model.GenericShip


/**
  * Sent to the player at the beginning of a game. He should create his fleet and send back the result
  *
  * @param nextActor The actor to call when the task is done
  * @param dimension The size of the grid
  * @param ships The fleet configuration he has to use
  */
class CreateFleet(val nextActor: ActorRef, val dimension: Dimension, val ships: Set[GenericShip])
