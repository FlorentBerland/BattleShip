package players

import akka.actor.Actor
import core.messages._
import ui.SwingUI

class HumanPlayer extends Actor{

  override def receive: Receive = {
    case msg: ChooseGameConfig => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: GameBegins => onGameBegins(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: NotifyHasBeenShot => onNotifyHasBeenShot(msg)
    case msg: GameOver => onGameOver(msg)

    case _ =>
  }

  private val _ui = new SwingUI(self)

  private def onChooseGameConfig(msg: ChooseGameConfig): Unit = {
    _ui.displayChoose(msg.nextActor)
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    _ui.displayCreateFleet(msg.nextActor, msg.dimension, msg.ships)
  }

  private def onGameBegins(msg: GameBegins): Unit = {
    _ui.displayGame(msg.fleet, msg.shotGrid)
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {

  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {

  }

  private def onNotifyHasBeenShot(msg: NotifyHasBeenShot): Unit = {

  }

  private def onGameOver(msg: GameOver): Unit = {

  }

}
