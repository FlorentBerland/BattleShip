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


  private def onStartGame(msg: StartGame): Unit = {
    // The players should not play in two games at the same time
    if(findGameByPlayer(msg.firstPlayer._1).isEmpty && findGameByPlayer(msg.otherPlayer._1).isEmpty){
      games = (BattleState(msg.firstPlayer, msg.otherPlayer), msg.sender) +: games
      msg.firstPlayer._1 ! new GameBegins(self, msg.firstPlayer._2, msg.otherPlayer._2.toOpponentGrid)
      msg.otherPlayer._1 ! new GameBegins(self, msg.otherPlayer._2, msg.firstPlayer._2.toOpponentGrid)
      msg.firstPlayer._1 ! new NotifyCanPlay(self, msg.otherPlayer._2.toOpponentGrid)
    }
  }

  private def onPlay(msg: Play): Unit = {
    val game = findGameByPlayer(msg.sender)
    if(game.isEmpty) return

    val battleState = game.get._1
    val replaysManager = game.get._2

    val (newState, newFleet, shotResult) = battleState.play(msg.sender, msg.shotCoordinates)
    shotResult match {
      case Success(_) =>
        newState.targetedTurn._1 ! new LastRoundResult(self, msg.shotCoordinates, shotResult, newFleet.toOpponentGrid)
        newState.nextTurn._1 ! new NotifyHasBeenShot(msg.shotCoordinates, newFleet)
        if(newState.nextTurn._2.isDestroyed){
          newState.targetedTurn._1 ! new GameOver(replaysManager, GameEnd.VICTORY, newFleet)
          newState.nextTurn._1 ! new GameOver(replaysManager, GameEnd.DEFEAT, newState.targetedTurn._2)
          replaysManager ! new GameFinished(self, newState.targetedTurn._1)
          games = games.filterNot(_ == game.get)
        } else {
          newState.nextTurn._1 ! new NotifyCanPlay(self, newState.targetedTurn._2.toOpponentGrid)
          games = (newState, replaysManager) +: games.filterNot(_ == game.get)
        }
      case Failure(_) =>
        msg.sender ! new LastRoundResult(self, msg.shotCoordinates, shotResult, newFleet.toOpponentGrid)
    }



  }


  /**
    * Find the game containing the given player
    *
    * @param player The player to retrieve
    * @return The game found
    */
  private def findGameByPlayer(player: ActorRef): Option[(BattleState, ActorRef)] =
    games.find(g => g._1.nextTurn._1 == player || g._1.targetedTurn._1 == player)
}