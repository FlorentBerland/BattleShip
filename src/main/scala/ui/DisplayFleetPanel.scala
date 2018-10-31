package ui

import java.awt.{Color, Dimension, Graphics, Point}

import core.model.{FleetGrid, Ship}
import javax.swing.JPanel
import util.FleetHelper

import scala.util.Random


/**
  * Render a fleet on a grid and manage basic selection operations. All the parameters
  * have their own accessors and are mutable to improve UI performance
  *
  * @param initFleet The fleet to display
  * @param dimensions The size of the grid in pixels
  * @param mTop The top margin
  * @param mBottom The bottom margin
  * @param mLeft The left margin
  * @param mRight The right margin
  */
class DisplayFleetPanel(initFleet: FleetGrid,
                        dimensions: Dimension,
                        mTop: Int,
                        mBottom: Int,
                        mLeft: Int,
                        mRight: Int
                       ) extends JPanel{

  private var _fleet = initFleet
  private var _dim = dimensions
  private var _mTop = mTop
  private var _mBottom = mBottom
  private var _mLeft = mLeft
  private var _mRight = mRight

  // Computed values caching
  private var _squareSize: Dimension = squareSize
  private var _shipColors: Map[Ship, Color] = fleet.ships.map(s => (s, computeShipColor(s))).toMap

  init()

  // Variables accessors
  def fleet: FleetGrid = _fleet
  def fleet_$eq(fleet: FleetGrid): Unit = {
    _fleet = fleet
    _shipColors = fleet.ships.map(s => (s, computeShipColor(s))).toMap
  }
  def dim: Dimension = _dim
  def dim_$eq(v: Dimension): Unit = {
    _dim = v
    _squareSize = squareSize
  }
  def marginTop: Int = _mTop
  def marginTop_$eq(v: Int): Unit = _mTop = v
  def marginBottom: Int = _mBottom
  def marginBottom_$eq(v: Int): Unit = _mBottom = v
  def marginLeft: Int = _mLeft
  def marginLeft_$eq(v: Int): Unit = _mLeft = v
  def marginRight: Int = _mRight
  def marginRight_$eq(v: Int): Unit = _mRight = v


  override def paint(g: Graphics): Unit = {
    //super.paint(g) // Cause blinking

    val flat = FleetHelper.flatten(fleet)
    flat.indices.foreach(i => {
      flat(i).indices.foreach(j => {
        flat(i)(j) match {
          case Some(ship) =>
            ship.squares.foreach(square => fillSquare(g, new Point(square._1.x, square._1.y), _shipColors(ship)))
          case None =>
            g.setColor(Color.white)
            g.fillRect(i*_squareSize.width + marginLeft, j*_squareSize.height + marginTop,
              _squareSize.width, _squareSize.height)
            g.setColor(Color.gray)
            g.drawRect(i*_squareSize.width + marginLeft, j*_squareSize.height + marginTop,
              _squareSize.width, _squareSize.height)
        }
      })
    })

    g.setColor(Color.black)
    fleet.shotsReceived.foreach(shot => {
      val left = shot.x*_squareSize.width + marginLeft
      val top = shot.y*_squareSize.height + marginTop
      val right = left + _squareSize.width
      val bottom = top + _squareSize.height
      g.drawLine(left, top, right, bottom)
      g.drawLine(left, bottom, right, top)
    })

  }


  /**
    * Return the square overlapped by the cursor
    *
    * @param mouseLocation The location relative to the panel
    * @return The square overlapped, between 0 and fleet size - 1
    */
  protected def squareCursor(mouseLocation: Point): Option[Point] = {
    if(mouseLocation == null)
      None
    else {
      val square = new Point((mouseLocation.x - marginLeft) / _squareSize.width,
        (mouseLocation.y - marginRight) / _squareSize.height)
      if (square.x < 0 || square.y < 0 || square.x >= initFleet.dim.width || square.y >= initFleet.dim.height)
        None
      else
        Some(square)
    }
  }


  /**
    * Draw a square in the delimited grid
    *
    * @param g The graphic context to use
    * @param square The square coordinates between 0 and fleet size - 1
    * @param color The fill color
    */
  protected def fillSquare(g: Graphics, square: Point, color: Color): Unit = {
    g.setColor(color)
    g.fillRect(square.x*_squareSize.width + marginLeft, square.y*_squareSize.height + marginTop,
      _squareSize.width, _squareSize.height)
  }


  /**
    * Compute a pseudo-random color for a ship
    *
    * @param s The ship to color
    * @return A color based on the ship hash code or black and white if the ship is sunk
    */
  protected def computeShipColor(s: Ship): Color = {
    // The color of an instance should not change
    // If it was a pure random, the color would change on every refresh, causing ships to blink
    val _random = new Random(s.hashCode())
    val R = _random.nextInt(192)
    val G = _random.nextInt(192)
    val B = _random.nextInt(192)
    if(!s.isDestroyed)
      new Color(R, G, B)
    else {
      val average = (R + G + B) / 3
      new Color(average, average, average)
    }
  }


  /**
    * Compute the squares size, depending on the grid size and the fleet dimensions
    */
  private def squareSize: Dimension = new Dimension(dim.width / fleet.dim.width, dim.height / fleet.dim.height)

  private def init(): Unit = {
    this.setPreferredSize(new Dimension(dim.width+marginLeft+marginRight, dim.height+marginTop+marginBottom))
  }

}
