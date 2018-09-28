package util

import core.model.{Ship, ShotGrid, ShotResult, FleetGrid}

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
    fleetGrid.ships.map(ship => ship.squares.map(square => matrix(square._1.x)(square._1.y) = Some(ship)))
    matrix
  }

  /**
    * Transform the set of ships model to a matrix containing results to the shots performed
    *
    * @param shotGrid The fleet to flatten
    * @return A flat version of the fleet
    */
  def flatten(shotGrid: ShotGrid): Array[Array[Option[ShotResult.Value]]] = {
    val matrix: Array[Array[Option[ShotResult.Value]]] = Array.fill(shotGrid.dim.width)(Array.fill(shotGrid.dim.height)(None))
    shotGrid.shotsPerformed.map(shot =>  matrix(shot._1.x)(shot._1.y) = Some(shot._2))
    matrix
  }


  /**
    * Return the longest sequence of values vertically aligned that match a predicate,
    *
    * @param matrix A matrix of booleans
    * @param predicate A matcher for the values
    * @return A matrix of longest sequences of true values found
    */
  def longestVerticalSequence[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] = {

    def longestSeq(row: List[T]): List[Int] = {
      if(row == Nil) Nil
      else if(row.tail == Nil) if(predicate(row.head)) 1 :: Nil else 0 :: Nil
      else {
        val seq = longestSeq(row.tail)
        if(predicate(row.head)) (seq.tail.head + 1) :: seq
        else 0 :: seq
      }
    }

    matrix.map(row => longestSeq(row.toList).toArray)
  }


  /**
    * Return the longest sequence of true values horizontally aligned for each square from left to right
    *
    * @param matrix A matrix of booleans
    * @param predicate A matcher for the values
    * @return A matrix of longest sequences of true values found
    */
  def longestHorizontalSequence[T](matrix: Array[Array[T]], predicate: T => Boolean): Array[Array[Int]] =
    longestVerticalSequence(matrix.transpose, predicate).transpose

}
