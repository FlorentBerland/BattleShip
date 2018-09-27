package core.messages

import java.awt.Point


/**
  * Notify a player that he has been shot by the opponent
  *
  * @param coordinates The coordinates of the shot
  */
class NotifyHasBeenShot(val coordinates: Point)