package model.battle

import java.awt.Dimension

/**
  * Represent a ship during the game. It is defined by a set of
  * squares and is sunk when all its squared are hit
  *
  * @param squares All the squares representing the ship, defined by a location and
  *                a status: true = alive, false = destroyed
  */
class PlayableShip(val squares: Set[(Dimension, Boolean)]) {

  /**
    * Indicate if the ship is destroyed, meaning all its
    * squares are destroyed
    *
    * @return True if the ship is sunk
    */
  def isDestroyed: Boolean = squares.forall(!_._2)


  /**
    * Return a ship with the hit applied on the current instance
    *
    * @param coordinates The coordinates of the shot
    * @return A ship with the hit applied. The structure is not modified if the shot misses the squares
    */
  def hit(coordinates: Dimension): PlayableShip =
    PlayableShip(squares.map(square => {
      if(square._1.width == coordinates.width && square._1.height == coordinates.height)
        (new Dimension(square._1.width, square._1.height), false)
      else
        square
    }))

}

object PlayableShip {
  def apply(squares: Set[(Dimension, Boolean)]): PlayableShip = new PlayableShip(squares)
}