package core

import akka.actor.{Actor, ActorRef}
import core.messages._

import scala.util.{Failure, Success}

/**
  * Manage the game and the rounds. This is the only class with an internal state and side effects
  * in its methods
  */
class BattleEngine extends Actor {

  override def receive: Receive = {
    case msg: StartGame => onStartGame(msg)
    case msg: Play => onPlay(msg)

    case _ =>
  }


  // Internal state:

  // Tuple: the state of a battle, the parent of the battle
  private var games = List.empty[(BattleState, ActorRef)]
  private var ids = Map.empty[String, ActorRef]


  private def onStartGame(msg: StartGame): Unit = {
    // Add the new game to the list of active games
    games = (BattleState(msg.firstPlayer, msg.otherPlayer), msg.sender) +: games
    ids = ids + (msg.firstPlayer._3 -> msg.firstPlayer._1, msg.otherPlayer._3 -> msg.otherPlayer._1)
    // Send the initial messages to the players
    msg.firstPlayer._1 ! new GameBegins(self, msg.firstPlayer._2, msg.otherPlayer._2.toOpponentGrid)
    msg.otherPlayer._1 ! new GameBegins(self, msg.otherPlayer._2, msg.firstPlayer._2.toOpponentGrid)
    msg.firstPlayer._1 ! new NotifyCanPlay(self, msg.otherPlayer._2.toOpponentGrid, msg.firstPlayer._3)
  }


  private def onPlay(msg: Play): Unit = {
    val game = findGameByPlayerUid(msg.playerUid)
    if(game.isEmpty) return

    val replaysManager = game.get._2

    val (newState, newFleet, shotResult) = game.get._1.play(msg.playerUid, msg.shotCoordinates)
    shotResult match {
      case Success(_) =>
        // Send the result to the player
        newState.targetedTurn._1 ! new LastRoundResult(self, msg.shotCoordinates, shotResult,
          newFleet.toOpponentGrid, newState.targetedTurn._3)
        // Notify the opponent of the shot performed
        newState.nextTurn._1 ! new NotifyHasBeenShot(msg.shotCoordinates, newFleet)
        if(newState.nextTurn._2.isDestroyed){
          // The game is finished, send the last messages to the actors
          newState.targetedTurn._1 ! new GameOver(replaysManager, GameEnd.VICTORY, newFleet, newState.targetedTurn._3)
          newState.nextTurn._1 ! new GameOver(replaysManager, GameEnd.DEFEAT, newState.targetedTurn._2, newState.nextTurn._3)
          replaysManager ! new GameFinished(newState.targetedTurn._3)
          // Remove the game and the references from the maps
          games = games.filterNot(_ == game.get)
          ids = ids.filterNot(id => id._1 == newState.nextTurn._3 || id._1 == newState.targetedTurn._3)
        } else {
          // Store the new state and remove the old one
          games = (newState, replaysManager) +: games.filterNot(_ == game.get)
          // Notify the next player to play
          newState.nextTurn._1 ! new NotifyCanPlay(self, newState.targetedTurn._2.toOpponentGrid, newState.nextTurn._3)
        }
      case Failure(_) =>
        ids(msg.playerUid) ! new LastRoundResult(self, msg.shotCoordinates, shotResult, newFleet.toOpponentGrid, msg.playerUid)
    }
  }


  /**
    * Find the game containing the given player uid
    *
    * @param uid The uid of the player to retrieve
    * @return The game found
    */
  private def findGameByPlayerUid(uid: String): Option[(BattleState, ActorRef)] =
    games.find(g => g._1.nextTurn._3 == uid || g._1.targetedTurn._3 == uid)

}