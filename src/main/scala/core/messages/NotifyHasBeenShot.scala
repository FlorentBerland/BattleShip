package core.messages

import java.awt.Point

import core.model.FleetGrid


/**
  * Notify a player that he has been shot by the opponent
  *
  * @param coordinates The coordinates of the shot
  * @param fleet The player's fleet after the opponent's round
  */
class NotifyHasBeenShot(val coordinates: Point, val fleet: FleetGrid)