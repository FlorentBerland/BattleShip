package ui

import java.awt.{Dimension, Point}

import core.model.GenericShip
import core.model.battle.ShotResult
import player.Player

import scala.util.Try

class PlayerConsole extends Player {

  override def createFleet(dim: Dimension, ships: Set[GenericShip]): Unit = ???

  override def lastRoundResult(result: Try[ShotResult.Value]): Unit = ???

  override def notifyHasBeenShot(coordinates: Point): Unit = ???

  override def notifyCanPlay(): Unit = ???

}
