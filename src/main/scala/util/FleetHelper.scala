package util

import core.model.{FleetGrid, Ship, ShotGrid, ShotResult}

import scala.reflect.ClassTag

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
    fleetGrid.ships.foreach(ship => ship.squares.foreach(square => {matrix(square._1.x - 1)(square._1.y - 1) = Some(ship)}))
    matrix
  }


  /**
    * Transform the shot grid model to a boolean matrix with true for the squares shot and false for the others
    *
    * @param shotGrid The grid to flatten
    * @return A boolean matrix of the shots
    */
  def flattenShotMap(shotGrid: ShotGrid): Array[Array[Boolean]] = {
    val matrix: Array[Array[Boolean]] =
      Array.fill(shotGrid.dim.width)(Array.fill(shotGrid.dim.height)(false))
    shotGrid.shotsPerformed.foreach(sp => matrix(sp.x - 1)(sp.y - 1) = true)
    matrix
  }


  /**
    * Return the longest sequence of values vertically aligned that match a predicate. It has to contain
    * at least one element
    *
    * @param matrix A matrix of booleans
    * @param predicate A matcher for the values
    * @return A matrix of longest sequences of true values found
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
    * @return A matrix of taxicab distances, 0 for the values that satisfied the predicate
    */
  def distanceToNearestObstacle[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] = {
    val computingDistances: Array[Array[Option[Int]]] = matrix.map(_.map(t => if(predicate(t))Some(0) else None))

    // In the worst case, the distances have to be computed the number of (biggest dimensions) times - 1
    (0 until (matrix.length max matrix.map(_.length).max)).foreach(_ => {
      matrix.indices.foreach(i => {
        matrix(i).indices.foreach(j => {
          matrix(i)(j) match {
            case None => // Needs to be computed: the value is the min of the neighbor values + 1
              val top: Option[Int] = if (i - 1 >= 0) computingDistances(i - 1)(j) else None
              val bottom: Option[Int] = if (i + 1 < matrix.length) computingDistances(i + 1)(j) else None
              val left: Option[Int] = if (j - 1 >= 0) computingDistances(i)(j - 1) else None
              val right: Option[Int] = if (j + 1 < matrix(i).length) computingDistances(i)(j + 1) else None
              computingDistances(i)(j) = min(top, min(bottom, min(left, right))).map(_ + 1)
            case _ =>
          }
        })
      })
    })

    def min(a: Option[Int], b: Option[Int]): Option[Int] = {
      if(a.isEmpty) b
      else if(b.isEmpty) a
      else Some(a.get min b.get)
    }

    computingDistances.map(_.map(_.getOrElse(-1)))
  }

}
