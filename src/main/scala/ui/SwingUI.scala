package ui

import java.awt._
import java.awt.event.{MouseEvent, MouseListener, WindowAdapter, WindowEvent}

import akka.actor.ActorRef
import core.messages.{QuitGame, UseGameConfig}
import core.model.GenericShip
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

  def displayCreateFleet(nextActor: ActorRef, dim: Dimension, expectedShips: Set[(GenericShip, Int)]): Unit = {
    this.getWindowListeners.map(wl => removeWindowListener(wl))
    this.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = nextActor ! new QuitGame(player)
    })
    refresh(new JPanel(){
      this.setLayout(new BorderLayout())
      this.add(new JPanel(){
        this.setBackground(Color.getColor("LightBlue"))
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS))
        expectedShips.map(es => (1 to es._2).map(i => this.add(button(es._1.name + " " + es._1.size, () => Unit))))
      }, BorderLayout.EAST)
      this.add(new JPanel(){
        this.setPreferredSize(new Dimension(400, 400))
      }, BorderLayout.CENTER)
      this.add(new JPanel(){
        this.setBackground(Color.darkGray)
        this.setLayout(new FlowLayout())
        this.add(button("Reset", () => Unit))
        this.add(button("Randomize", () => Unit))
        this.add(button("Confirm", () => Unit))
      }, BorderLayout.NORTH)
      this.add(button("QuitGame", () => nextActor ! new QuitGame(player)), BorderLayout.SOUTH)
    })
  }

  private def init(): Unit = {
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


  private def button(displayName: String, cb: () => Unit): JButton = {
    new JButton(displayName){
      private val self: JButton = this
      this.setBackground(Color.white)
      this.setBorder(new EmptyBorder(10, 10, 10, 10))
      this.addMouseListener(new MouseListener {
        override def mouseClicked(e: MouseEvent): Unit = { cb() }
        override def mousePressed(e: MouseEvent): Unit = {}
        override def mouseReleased(e: MouseEvent): Unit = {}
        override def mouseEntered(e: MouseEvent): Unit = { self.setBackground(Color.gray)}
        override def mouseExited(e: MouseEvent): Unit = { self.setBackground(Color.white)}
      })
    }
  }

}
