import java.awt.Point

import core.model.{FleetGrid, Ship}
import util.{DefaultGameConfig, FleetHelper}

object Main extends App {


  var fleet = FleetGrid(DefaultGameConfig.dimensions, DefaultGameConfig.ships)
  val shotFleet = FleetGrid(fleet.dim, fleet.ships, Set(new Point(9, 9)))
  printArray(FleetHelper.distanceToNearestObstacle[Boolean](FleetHelper.flattenShotMap(shotFleet.toOpponentGrid), p => p))

  def printArray[T](array: Array[Array[T]]): Unit = {
    array.transpose.toList.foreach(row => { row.foreach(a => print(a + "\t")); println() })
  }

}
