package util

import core.model._

import scala.annotation.tailrec

/**
  * This object provides functions to help for advanced fleet operations
  */
object FleetHelper {

  /**
    * Transform the set of ships model to a matrix containing references to the ships on every square
    *
    * @param fleetGrid The fleet to flatten
    * @return A flat version of the fleet
    */
  def flatten(fleetGrid: FleetGrid): Array[Array[Option[Ship]]] = {
    val matrix: Array[Array[Option[Ship]]] = Array.fill(fleetGrid.dim.width)(Array.fill(fleetGrid.dim.height)(None))
    fleetGrid.ships.foreach(ship => ship.squares.foreach(square => {matrix(square._1.x)(square._1.y) = Some(ship)}))
    matrix
  }


  /**
    * Transform the set of ships model to a matrix containing references to the ships on every square
    *
    * @param shotGrid The grid to flatten
    * @return A flat version of the ships squares hit
    */
  def flatten(shotGrid: ShotGrid): Array[Array[Option[Ship]]] = flatten(shotGrid.toFleetGrid)


  /**
    * Transform the shot grid model to a boolean matrix with true for the squares shot and false for the others
    *
    * @param shotGrid The grid to flatten
    * @return A boolean matrix of the shots
    */
  def flattenShotMap(shotGrid: ShotGrid): Array[Array[Boolean]] = {
    val matrix: Array[Array[Boolean]] =
      Array.fill(shotGrid.dim.width)(Array.fill(shotGrid.dim.height)(false))
    shotGrid.shotsPerformed.foreach(sp => matrix(sp.x)(sp.y) = true)
    matrix
  }


  /**
    * Returns the ships alive in a shot grid from an real composition to help aiming at the fleet
    *
    * @param composition The real composition of the fleet
    * @param shotGrid The grid from where to retrieve the ships
    * @return The ships not sunk based on the real composition
    */
  @tailrec
  def notSunkShips(composition: Set[GenericShip], shotGrid: ShotGrid): Set[GenericShip] = {
    if(shotGrid.shipsFound.isEmpty) composition
    else composition.find(_.size == shotGrid.shipsFound.head.squares.size) match {
      case Some(genShipDestroyed) => // The head ship was in the composition, its removed from it
        notSunkShips(composition - genShipDestroyed, ShotGrid(shotGrid.dim, shotGrid.shipsFound.tail, shotGrid.shotsPerformed))
      case None => // The head ship in the shot grid was not in the composition, so its only partially destroyed
        notSunkShips(composition, ShotGrid(shotGrid.dim, shotGrid.shipsFound.tail, shotGrid.shotsPerformed))
    }
  }


  /**
    * Return the longest sequence of values vertically aligned that match a predicate. It has to contain
    * at least one element
    *
    * @param matrix A matrix of booleans
    * @param predicate A matcher for the values
    * @return A matrix of longest sequences of true values found
    * @example [[ false, true, true, false, false, true, true, true]] -> [[0, 2, 1, 0, 0, 3, 2, 1]]
    */
  def longestVerticalSequence[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] = {

    def longestSeq(row: List[T]): List[Int] = {
      if(row == Nil) Nil
      else if(row.tail == Nil) { if(predicate(row.head)) 1 :: Nil else 0 :: Nil }
      else {
        val seq = longestSeq(row.tail)
        if(predicate(row.head)) (seq.head + 1) :: seq
        else 0 :: seq
      }
    }

    matrix.map(row => longestSeq(row.toList).toArray)
  }


  /**
    * Return the longest sequence of true values horizontally aligned for each square from left to right
    *
    * @param matrix A matrix of values to convert
    * @param predicate A matcher for the values
    * @return A matrix of longest sequences of true values found
    */
  def longestHorizontalSequence[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] =
    longestVerticalSequence(matrix.transpose, predicate).transpose


  /**
    * Return a matrix with the same dimensions as the matrix parameter. Each value of the matrix is the distance
    * to the nearest value in the original matrix that satisfies the predicate
    *
    * @param matrix The matrix of values to evaluate
    * @param predicate The filter
    * @return A matrix of taxicab distances, 0 for the values that satisfied the predicate or a matrix or 0 if
    *         none satisfied the predicate
    */
  def distanceToNearestObstacle[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] = {
    val computingDistances: Array[Array[Option[Int]]] = matrix.map(_.map(t => if(predicate(t)) Some(0) else None))

    // In the worst case, the distances have to be computed the number of (sum over dimensions-1) times
    (0 until (computingDistances.length + computingDistances.map(_.length).max - 2)).foreach(_ => {
      computingDistances.indices.foreach(i => {
        computingDistances(i).indices.foreach(j => {
          computingDistances(i)(j) match {
            case Some(0) => // The reference variable, should not be changed
            case _ => // Needs to be computed: the value is the min of the neighbor values + 1
              val top: Option[Int] = if (i - 1 >= 0) computingDistances(i - 1)(j) else None
              val bottom: Option[Int] = if (i + 1 < matrix.length) computingDistances(i + 1)(j) else None
              val left: Option[Int] = if (j - 1 >= 0) computingDistances(i)(j - 1) else None
              val right: Option[Int] = if (j + 1 < matrix(i).length) computingDistances(i)(j + 1) else None
              computingDistances(i)(j) = min(top, min(bottom, min(left, right))).map(_ + 1)
          }
        })
      })
    })

    def min(a: Option[Int], b: Option[Int]): Option[Int] = {
      if(a.isEmpty) b
      else if(b.isEmpty) a
      else Some(a.get min b.get)
    }

    computingDistances.map(_.map(_.getOrElse(0)))
  }

  // TODO : Delete it
  def printArray[T](array: Array[Array[T]]): Unit = {
    array.transpose.toList.foreach(row => { row.foreach(a => print(a + "\t")); println() })
    println()
  }


  /**
    * Returns the max value of a matrix of integers with its indexes
    *
    * @param matrix The given matrix
    * @return The max value, its first and its second index
    * @example maxValue(Array(Array(1,3,2), Array(4,1,3))) = (4, 1, 0)
    */
  def maxValue(matrix: Array[Array[Int]]): (Int, Int, Int) = {
    val maxVal = matrix.maxBy(_.max).max
    val x = matrix.indexWhere(_.max == maxVal)
    val y = matrix(x).indexWhere(_ == maxVal)
    (maxVal, x, y)
  }

}
