package players

import akka.actor.Actor
import core.messages._
import ui.SwingUI


/**
  * A human player, using a Swing GUI. The player is asked to choose his opponent before playing the game
  */
class HumanPlayer extends Actor{

  override def receive: Receive = {
    case msg: ChooseOpponent => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: GameBegins => onGameBegins(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: NotifyHasBeenShot => onNotifyHasBeenShot(msg)
    case msg: GameOver => onGameOver(msg)

    case _ =>
  }

  override def postStop(): Unit = {
    _ui.dispose()
    super.postStop()
  }

  private val _ui = new SwingUI(self)

  private def onChooseGameConfig(msg: ChooseOpponent): Unit = {
    _ui.displayChoose(msg.nextActor)
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    _ui.displayCreateFleet(msg.nextActor, msg.dimension, msg.ships, msg.playerId)
  }

  private def onGameBegins(msg: GameBegins): Unit = {
    _ui.displayGame(msg.nextActor, msg.fleet, msg.shotGrid, "")
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    _ui.notifiedToPlay(msg.nextActor, msg.shotGrid, msg.playerId)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    _ui.notifiedLastRoundResult(msg.shotGrid, msg.result, msg.playerId)
  }

  private def onNotifyHasBeenShot(msg: NotifyHasBeenShot): Unit = {
    _ui.notifiedHasBeenShot(msg.fleet)
  }

  private def onGameOver(msg: GameOver): Unit = {
    _ui.notifiedGameOver(msg.sender, msg.end, msg.opponentFleet, msg.playerId)
  }

}
