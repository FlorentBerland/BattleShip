package players

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.{FleetGrid, GenericShip, ShotGrid}
import util.FleetHelper

import scala.util.Failure


/**
  * AI that should shot smartly at the opponent. It will choose first the squares that has the
  * greatest probability of containing a ship. Chooses a medium AI as opponent if asked to.
  */
class StrongAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: ChooseGameConfig => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: GameBegins => onGameBegins(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)

    case _ =>
  }


  private var aliveShips = Set.empty[(GenericShip, Boolean)]


  private def onChooseGameConfig(msg: ChooseGameConfig): Unit = {
    msg.nextActor ! new UseGameConfig(self, "MediumAIPlayer")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(self, FleetGrid(msg.dimension, msg.ships))
    aliveShips = msg.ships.map((_, true))
  }

  private def onGameBegins(msg: GameBegins): Unit = {
    // TODO: Reset the AI state
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    play(msg.nextActor, msg.shotGrid)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    msg.result match {
      case Failure(_) => play(msg.sender, msg.shotGrid)
      case _ =>
    }
  }

  private def play(sender: ActorRef, shotGrid: ShotGrid): Unit = {
    // Compute the greatest empty sequences of squares not shot and remove the
    // sequences that cannot contain the alive ships
    val shotMap = FleetHelper.flattenShotMap(shotGrid)
    val verticalSequences = FleetHelper.longestVerticalSequence[Boolean](shotMap, a => !a)
    val horizontalSequences = FleetHelper.longestHorizontalSequence[Boolean](shotMap, a => !a)
  }

}
