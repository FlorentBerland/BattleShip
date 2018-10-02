package ui.playing

import java.awt.event.{MouseEvent, MouseListener, MouseMotionListener}
import java.awt.{Color, Dimension, Graphics, Point}

import akka.actor.ActorRef
import core.messages.Play
import core.model.ShotGrid
import ui.DisplayFleetPanel


/**
  * Manages the display of the user's fleet during the game
  *
  * @param initShotGrid The fleet to display
  * @param dimensions The size of the grid
  * @param mTop The top margin
  * @param mBottom The bottom margin
  * @param mLeft The left margin
  * @param mRight The right margin
  */
class GameFleetPanel(initShotGrid: ShotGrid,
                     dimensions: Dimension,
                     mTop: Int,
                     mBottom: Int,
                     mLeft: Int,
                     mRight: Int,
                     player: ActorRef,
                     nextActor: ActorRef
                        ) extends DisplayFleetPanel(initShotGrid.toHeuristicFleetGrid, dimensions, mTop, mBottom, mLeft, mRight)
  with MouseListener with MouseMotionListener
{

  private var _shotGrid: ShotGrid = initShotGrid
  private var _nextActor: ActorRef = nextActor

  init()

  def shotGrid: ShotGrid = _shotGrid
  def shotGrid_$eq(sg: ShotGrid): Unit = {
    _shotGrid = sg
    fleet = _shotGrid.toHeuristicFleetGrid
  }
  def cbActor: ActorRef = _nextActor
  def cbActor_$eq(cbA: ActorRef): Unit = _nextActor = cbA

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
        squareCursor(getMousePosition).foreach(point => cbActor ! new Play(player, new Point(point.x+1, point.y+1)))
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
