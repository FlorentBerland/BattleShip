package ui

import java.awt.event.{MouseEvent, MouseListener, MouseMotionListener}
import java.awt.{Color, Dimension, Graphics}

import core.model.FleetGrid


/**
  * Manages the display of the user's fleet during the game
  *
  * @param initFleet The fleet to display
  * @param dimensions The size of the grid
  * @param mTop The top margin
  * @param mBottom The bottom margin
  * @param mLeft The left margin
  * @param mRight The right margin
  */
class GameFleetPanel(initFleet: FleetGrid,
                         dimensions: Dimension,
                         mTop: Int,
                         mBottom: Int,
                         mLeft: Int,
                         mRight: Int
                        ) extends DisplayFleetPanel(initFleet, dimensions, mTop, mBottom, mLeft, mRight)
  with MouseListener with MouseMotionListener
{

  init()

  private def init(): Unit = {
    this.addMouseMotionListener(this)
    this.addMouseListener(this)
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)

    squareCursor(this.getMousePosition).foreach(fillSquare(g, _, Color.gray))
  }

  // Mouse events part

  override def mouseMoved(e: MouseEvent): Unit = {
    paint(this.getGraphics)
  }

  override def mouseClicked(e: MouseEvent): Unit = {
    e.getButton match {
      case 1 => // Left click
      case 3 => // Right click
      case _ =>
    }
  }


  // Useless events
  override def mouseDragged(e: MouseEvent): Unit = {}
  override def mouseEntered(e: MouseEvent): Unit = {}
  override def mouseExited(e: MouseEvent): Unit = {}
  override def mousePressed(e: MouseEvent): Unit = {}
  override def mouseReleased(e: MouseEvent): Unit = {}

}
