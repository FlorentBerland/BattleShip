package core.model

import java.awt.Point

/**
  * Represent a ship during the game. It is defined by a set of
  * squares and is sunk when all its squared are hit
  *
  * @param squares All the squares representing the ship, defined by a location and
  *                a status: true = alive, false = destroyed
  */
class Ship(val squares: Set[(Point, Boolean)]) {

  /**
    * Return a ship with the shot applied on the current instance
    *
    * @param coordinates The coordinates of the shot
    * @return A ship with the hit result on it. The ship structure is not modified if the shot misses the squares
    */
  def shot(coordinates: Point): Ship =
    Ship(squares.map(square => {
      if(square._1.equals(coordinates))
        (square._1, false)
      else
        square
    }))


  /**
    * Count the number of alive squares of the ship (the squares not hit)
    */
  def aliveSquares: Int = squares.count(_._2)


  /**
    * Test if the ship is destroyed, meaning all its
    * squares are destroyed
    *
    * @return True if the ship is sunk
    */
  def isDestroyed: Boolean = aliveSquares == 0

}

object Ship {
  def apply(squares: Set[(Point, Boolean)]): Ship = new Ship(squares)
}