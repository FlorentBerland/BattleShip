package ui

import java.awt._
import java.awt.event.{MouseEvent, MouseListener}

import akka.actor.ActorRef
import core.GameEnd
import core.messages.QuitGame
import core.model.{FleetGrid, GenericShip, ShotGrid, ShotResult}
import javax.swing._
import javax.swing.border.EmptyBorder
import ui.creating.CreatingComponent
import ui.ending.GameOverComponent
import ui.initializing.ChooseOpponentComponent
import ui.playing.GameComponent

import scala.util.Try

class SwingUI(val player: ActorRef) extends JFrame {

  private var _currentDisplayedComponent: Component = _

  init()

  def displayChoose(nextActor: ActorRef): Unit = {
    this.refresh(new ChooseOpponentComponent(player, nextActor))
  }

  def displayCreateFleet(nextActor: ActorRef, dim: Dimension, expectedShips: Set[GenericShip], id: String): Unit = {
    this.refresh(new CreatingComponent(id, nextActor, dim, expectedShips))
  }

  def displayGame(nextActor: ActorRef, fleetGrid: FleetGrid, shotGrid: ShotGrid, playerId: String): Unit = {
    this.refresh(new GameComponent(playerId, nextActor, fleetGrid, shotGrid))
  }

  def notifiedHasBeenShot(fleetGrid: FleetGrid): Unit = {
    _currentDisplayedComponent match {
      case _gp: GameComponent =>
        _gp.notifiedHasBeenShot(fleetGrid)
      case _ =>
    }
  }

  def notifiedToPlay(nextActor: ActorRef, shotGrid: ShotGrid, playerId: String): Unit = {
    _currentDisplayedComponent match {
      case _gp: GameComponent =>
        _gp.notifiedToPlay(nextActor, shotGrid, playerId)
      case _ =>
        nextActor ! new QuitGame(playerId)
    }
  }

  def notifiedLastRoundResult(shotGrid: ShotGrid, result: Try[ShotResult.Value], id: String): Unit = {
    _currentDisplayedComponent match {
      case _gp: GameComponent =>
        _gp.notifiedLastRoundResult(shotGrid, result, id)
      case _ =>
    }
  }

  def notifiedGameOver(nextActor: ActorRef, end: GameEnd.End, opponentGrid: FleetGrid, id: String): Unit = {
    this.refresh(new GameOverComponent(id, nextActor, end, opponentGrid))
  }

  private def init(): Unit = {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    this.setVisible(true)
    refresh(new JPanel())
    center()
    this.requestFocus()
  }

  private def refresh(comp: Component): Unit = {
    if(_currentDisplayedComponent != null) remove(_currentDisplayedComponent)
    _currentDisplayedComponent = comp
    add(_currentDisplayedComponent)
    this.pack()
    center()
  }

  private def center(): Unit = {
    this.setLocation(new Point((Toolkit.getDefaultToolkit.getScreenSize.width-this.getWidth)/2,
      (Toolkit.getDefaultToolkit.getScreenSize.height-this.getHeight)/2))
  }

}

object SwingUI {

  /**
    * Create a JButton to add on the UI
    *
    * @param displayName The button text
    * @param cb The controller to invoke when the button is clicked
    * @return The button
    */
  def button(displayName: String, cb: () => Unit): JButton = {
    new JButton(displayName){
      private val self: JButton = this
      this.setBackground(Color.white)
      this.setBorder(new EmptyBorder(10, 10, 10, 10))
      this.addMouseListener(new MouseListener {
        override def mouseClicked(e: MouseEvent): Unit = { cb() }
        override def mousePressed(e: MouseEvent): Unit = {}
        override def mouseReleased(e: MouseEvent): Unit = {}
        override def mouseEntered(e: MouseEvent): Unit = { self.setBackground(new Color(180, 185, 240))}
        override def mouseExited(e: MouseEvent): Unit = { self.setBackground(Color.white)}
      })
    }
  }

}