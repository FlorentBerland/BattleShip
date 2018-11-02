package ui.creating

import java.awt.{BorderLayout, Color, Dimension, FlowLayout}

import akka.actor.ActorRef
import core.messages.FleetCreated
import core.model.{FleetGrid, GenericShip, Ship}
import javax.swing.{BoxLayout, JLabel, JPanel}
import ui.SwingUI


/**
  * Manages the fleet creation
  *
  * @param playerId The player id to be known by the game
  * @param nextActor The actor to notify when the fleet is created
  * @param dim The grid dimensions
  * @param expectedShips The ships to place
  */
class CreatingComponent(
                         val playerId: String,
                         val nextActor: ActorRef,
                         val dim: Dimension,
                         val expectedShips: Set[GenericShip]
                       ) extends JPanel {

  init()

  private def init(): Unit = {
    val editPanel = new FleetCreationPanel(FleetGrid(dim, Set.empty, Set.empty), new Dimension(300, 300), 10, 10, 10, 10)
    this.setLayout(new BorderLayout())

    // Ship buttons panel
    this.add(new JPanel(){
      this.setBackground(Color.getColor("LightBlue"))
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS))
      expectedShips.map(es => this.add(SwingUI.button(es.name + " (" + es.size + ")",
        () => editPanel.dragShip(es, true,
          (ship: Option[Ship]) => ship.foreach(s => {
            val newFleet = editPanel.fleet + s
            if(newFleet.isValid){
              editPanel.fleet = newFleet
            }
          })))))
    }, BorderLayout.EAST)

    // Central panel (grid)
    this.add(editPanel, BorderLayout.CENTER)

    // Header panel
    this.add(new JPanel(){
      this.setBackground(new Color(100, 110, 200))
      this.add(new JLabel("Prepare your fleet"){ this.setForeground(Color.white) })
    }, BorderLayout.NORTH)

    // Footer panel
    this.add(new JPanel(){
      this.setLayout(new FlowLayout())
      this.add(new JLabel("Right click while dragging to flip the ship    "))
      this.add(SwingUI.button("Clear all", () => {
        editPanel.fleet = FleetGrid(dim, Set.empty, Set.empty)
        editPanel.paint(editPanel.getGraphics)
      }))
      this.add(SwingUI.button("Randomize", () => {
        editPanel.fleet = FleetGrid(dim, expectedShips)
        editPanel.paint(editPanel.getGraphics)
      }))
      this.add(SwingUI.button("Ready !", () => nextActor ! new FleetCreated(playerId, editPanel.fleet)))
    }, BorderLayout.SOUTH)
  }

}
