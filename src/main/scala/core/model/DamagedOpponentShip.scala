package core.model

import java.awt.Point


/**
  * A ship representation for the opponent when it is damaged (not sunk).
  * The problem solved by this class was every ship square destroyed was represented
  * to the player as a single square ship to hide the real ship positions if they are side by side.
  * So the all the squares hit were represented as a ship sunk because its only square was hit. This
  * class represents the single square ship representation but keeps the not-sunk information.
  */
class DamagedOpponentShip(squares: Set[(Point, Boolean)]) extends Ship(squares){
  override def isDestroyed: Boolean = false
}

object DamagedOpponentShip {
  def apply(squares: Set[(Point, Boolean)]): DamagedOpponentShip = new DamagedOpponentShip(squares)
}