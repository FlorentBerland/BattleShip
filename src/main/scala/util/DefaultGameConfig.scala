package util

import java.awt.Dimension

import core.model.GenericShip


object DefaultGameConfig {
  def dimensions = new Dimension(10, 10)
  def ships: Set[GenericShip] = Set(
    new GenericShip("Carrier", 5),
    new GenericShip("Battleship", 4),
    new GenericShip("Cruiser", 3),
    new GenericShip("Submarine", 3),
    new GenericShip("Destroyer", 2)
  )
  def replays: Int = 100
}
