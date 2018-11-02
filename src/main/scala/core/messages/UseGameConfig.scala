package core.messages

import java.awt.Dimension

import akka.actor.ActorRef
import core.model.GenericShip


/**
  * Contains the game game the the GameReplaysManager should use
  *
  * @param sender The sender of the message
  * @param player1 The first player
  * @param player2 The second player
  * @param dimensions The dimensions of the grid
  * @param ships The ships to place
  * @param replays The number of rounds max
  */
class UseGameConfig(val sender: ActorRef,
                    val player1: ActorRef,
                    val player2: ActorRef,
                    val dimensions: Dimension,
                    val ships: Set[GenericShip],
                    val replays: Int)
