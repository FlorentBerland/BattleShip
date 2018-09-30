package ui

import java.awt._
import java.awt.event.{MouseEvent, MouseListener, WindowAdapter, WindowEvent}

import akka.actor.ActorRef
import core.messages.{QuitGame, UseGameConfig}
import core.model.{FleetGrid, GenericShip, Ship}
import javax.swing._
import javax.swing.border.EmptyBorder

class SwingUI(val player: ActorRef) extends JFrame {

  private var _currentDisplayedComponent: Component = _

  init()

  def displayChoose(nextActor: ActorRef): Unit = {
    this.getWindowListeners.map(wl => removeWindowListener(wl))
    this.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = nextActor ! new QuitGame(player)
    })

    this.refresh(new JPanel(){
      this.setLayout(new BorderLayout())
      this.add(new JLabel("Choose your opponent"), BorderLayout.NORTH)
      this.add(new JPanel(){
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS))
        this.add(button("Human player",
          () => nextActor ! new UseGameConfig(player, "HumanPlayer")))
        this.add(button("Weak AI",
          () => nextActor ! new UseGameConfig(player, "WeakAIPlayer")))
        this.add(button("Medium AI",
          () => nextActor ! new UseGameConfig(player, "MediumAIPlayer")))
        this.add(button("Strong AI",
          () => nextActor ! new UseGameConfig(player, "StrongAIPlayer")))
      }, BorderLayout.CENTER)
      this.add(button("QuitGame", () => nextActor ! new QuitGame(player)), BorderLayout.SOUTH)
    })
  }

  def displayCreateFleet(nextActor: ActorRef, dim: Dimension, expectedShips: Set[GenericShip]): Unit = {
    this.getWindowListeners.map(wl => removeWindowListener(wl))
    this.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = nextActor ! new QuitGame(player)
    })
    refresh(new JPanel(){
      var editPanel = new FleetCreationPanel(FleetGrid(dim, Set.empty, Set.empty))
      this.setLayout(new BorderLayout())

      // Ship buttons panel
      this.add(new JPanel(){
        this.setBackground(Color.getColor("LightBlue"))
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS))
        expectedShips.map(es => this.add(button(es.name + " " + es.size,
          () => editPanel.dragShip(es, true,
            (ship: Option[Ship]) => ship.map(s => {
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
        this.add(button("Clear all", () => {
          editPanel.fleet = FleetGrid(dim, Set.empty, Set.empty)
          editPanel.paint(editPanel.getGraphics)
        }))
        this.add(button("Randomize", () => {
          editPanel.fleet = FleetGrid(dim, expectedShips)
          editPanel.paint(editPanel.getGraphics)
        }))
        this.add(button("Ready !", () => Unit))
      }, BorderLayout.SOUTH)
    })
  }

  private def init(): Unit = {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    this.setVisible(true)
    refresh(new JPanel())
  }

  private def refresh(comp: Component): Unit = {
    if(_currentDisplayedComponent != null) remove(_currentDisplayedComponent)
    _currentDisplayedComponent = comp
    add(_currentDisplayedComponent)
    this.pack()
    this.setLocation(new Point((Toolkit.getDefaultToolkit.getScreenSize.width-this.getWidth)/2,
      (Toolkit.getDefaultToolkit.getScreenSize.height-this.getHeight)/2))
  }


  /**
    * Create a JButton to add on the UI
    *
    * @param displayName The button text
    * @param cb The controller to invoke when the button is clicked
    * @return The button
    */
  private def button(displayName: String, cb: () => Unit): JButton = {
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
