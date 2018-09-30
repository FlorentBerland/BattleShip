package core.model

import java.awt.{Dimension, Point}

import util.{FleetHelper, Rand}

import scala.util.{Failure, Random, Success, Try}

/**
  * The current fleet of a player
  *
  * @param dim The size of the grid
  * @param ships The ships of the player
  * @param shotsReceived A set of shot coordinates performed on this grid
  */
class FleetGrid(val dim: Dimension, val ships: Set[Ship], val shotsReceived: Set[Point]) {

  /**
    * Return a fleet grid with the shot applied on the current instance
    *
    * @param coordinates The coordinates of the shot
    * @return Success with a fleet with the shot applied. Unmodified if the shot miss. Return
    *         Failure if the shot cannot be performed
    */
  def shot(coordinates: Point): Try[(FleetGrid, ShotResult.Result)] = {
    if(!canShot(coordinates))
      Failure(new IllegalArgumentException("Cannot shoot at coordinates " + coordinates))
    else {
      val resultByShip: Set[(Ship, ShotResult.Value)] = ships.map(ship => {
        val shotShip = ship shot coordinates
        (shotShip,
          if (ship.aliveSquares != shotShip.aliveSquares) {
            if (shotShip.isDestroyed)
              ShotResult.HIT_AND_SINK
            else
              ShotResult.HIT
          } else ShotResult.MISS)
      })
      Success((FleetGrid(dim, resultByShip.map(_._1), shotsReceived + coordinates), resultByShip.map(_._2).max))
    }
  }


  /**
    * Test whether the shot is correct (meaning it is in the grid and the square has not already been shot)
    *
    * @param coordinates The coordinates of the shot
    * @return True if the shot can be performed
    */
  def canShot(coordinates: Point): Boolean =
    coordinates.x > 0 && coordinates.y > 0 &&
    coordinates.x <= dim.width && coordinates.y <= dim.height &&
    shotsReceived.forall(square => square.x != coordinates.x || square.y != coordinates.y)


  /**
    * Convert the instance into a grid to be sent to the opponent
    */
  def toShotGrid: ShotGrid = {
    ShotGrid(dim, shotsReceived.map(s => (s, ShotResult.HIT))) // FIXME
  }


  /**
    * Indicate if all the ships are destroyed, meaning the battle is lost for the
    * FleetGrid owner
    *
    * @return True if all the ships are destroyed, false otherwise
    */
  def isDestroyed: Boolean = ships.forall(_.isDestroyed)


  /**
    * Tests whether the ships are correctly placed on the grid
    *
    * @return True if the fleet abide the game ship placing rules
    */
  def isValid: Boolean = {
    // Every ship has to be in the grid:
    if(!ships.forall(_.squares.forall(s => s._1.x > 0 && s._1.y > 0 && s._1.x <= dim.width && s._1.y <= dim.height)))
      return false

    // The ships has to be aligned horizontally or vertically:
    if(!ships.forall(ship => {
      // Push all the squares of the ship in two sets, one for the x and one for the y
      val coordsSets = ship.squares.foldLeft((Set.empty[Int], Set.empty[Int]))((tuple, square) => {
        (tuple._1 + square._1.x, tuple._2 + square._1.y)
      })
      // If the ship is aligned, one set must be size 1 and the other one must be greater than 1
      (coordsSets._1.size == 1 && coordsSets._2.size > 1) || (coordsSets._1.size > 1 && coordsSets._2.size == 1)
    }))
      return false

    // The ships should not overlap each other:
    // TODO

    // The ships should not be split:
    // TODO

    true
  }


  /**
    * Add a ship to the fleet
    *
    * @param ship The ship to add
    * @return A fleet with the new ship
    */
  def +(ship: Ship): FleetGrid = FleetGrid(dim, ships + ship, shotsReceived)

}

object FleetGrid {

  private val _rand = new Random()

  def apply(dim: Dimension, ships: Set[Ship], shotsReceived: Set[Point]) = new FleetGrid(dim, ships, shotsReceived)

  /**
    * Randomly generates a fleet based on the given configuration
    *
    * @param dim The dimensions of the grid
    * @param expectedShips The fleet composition
    * @return A new fleet
    */
  def apply(dim: Dimension, expectedShips: Set[GenericShip]): FleetGrid = {
    var fleet = new FleetGrid(dim, Set.empty, Set.empty)
    if(expectedShips.isEmpty)
      return fleet

    // Randomly place the first ship
    val firstShip: Ship = Rand.r.nextInt(2) match {
      case 0 => // Horizontal
        val firstPoint =
          new Point(Rand.r.nextInt(dim.width - expectedShips.head.size) + 1, Rand.r.nextInt(dim.height) + 1)
        Ship((0 until expectedShips.head.size).map(i => (new Point(i + firstPoint.x, firstPoint.y), true)).toSet)
      case 1 => // Vertical
        val firstPoint =
          new Point(Rand.r.nextInt(dim.width) + 1, Rand.r.nextInt(dim.height - expectedShips.head.size) + 1)
        Ship((0 until expectedShips.head.size).map(i => (new Point(firstPoint.x, i + firstPoint.y), true)).toSet)
    }
    fleet = FleetGrid(dim, Set(firstShip), Set.empty)

    // Place the other ships in order to have the best distribution
    expectedShips.tail.foreach(ship => {
      val flatFleet = FleetHelper.flatten(fleet)

      // Compute the longest sequences of vertical or horizontal empty square sequences
      val (arrayOfLongestSequence, isHorizontal) = Rand.r.nextInt(2) match {
        case 0 =>
          (FleetHelper.longestHorizontalSequence[Option[Ship]](flatFleet, _.isEmpty), true)
        case 1 =>
          (FleetHelper.longestVerticalSequence[Option[Ship]](flatFleet, _.isEmpty), false)
      }

      val longestSeq: Int = arrayOfLongestSequence.map(_.max).max
      // x and y are the array coordinates of the longest sequence of empty squares found
      val x = arrayOfLongestSequence.indexWhere(row => row.max == longestSeq)
      val y = arrayOfLongestSequence(x).indexWhere(_ == longestSeq)

      // Now, find the middle of this empty seq to center the ship on it
      // Ideal value is middle of the sequence - (ship size / 2)
      val newShip: Ship = if(isHorizontal){
        val firstPoint = new Point(x + longestSeq/2 - ship.size/2 + 1, y + 1)
        Ship((0 until ship.size).map(i => (new Point(firstPoint.x + i, firstPoint.y), true)).toSet)
      } else {
        val firstPoint = new Point(x + 1, y + longestSeq/2 - ship.size/2 + 1)
        Ship((0 until ship.size).map(i => (new Point(firstPoint.x, firstPoint.y + i), true)).toSet)
      }

      // Add it to the fleet
      fleet = FleetGrid(dim, fleet.ships + newShip, fleet.shotsReceived)
    })

    fleet
  }

  def printArray[T](array: Array[Array[T]]): Unit = {
    array.transpose.toList.foreach(row => { row.foreach(a => print(a + "\t")); println() })
  }
}

