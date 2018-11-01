package core

import java.awt.Dimension

import akka.actor.{Actor, ActorRef, Props}
import core.messages._
import core.model.{FleetGrid, GenericShip}


class GameFleetManager extends Actor {

  override def receive: Receive = {
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: FleetCreated => onFleetCreated(msg)
    case msg: GameFinished => onGameFinished(msg)
    case msg: Replay => onReplay(msg)
    case msg: QuitGame => onQuitGame(msg)

    case _ =>
  }


  private val _gameEngine: ActorRef = context.actorOf(Props[BattleEngine])
  private val _retries = 100

  ///// Manager state

  // Should be set only once:
  private var _fleetComposition: Set[GenericShip] = _
  private var _dimensions: Dimension = _
  private var _parent: ActorRef = _

  // Updated after every game:
  private var _player1 = new Player()
  private var _player2 = new Player()
  private var _lastPlayer = _player2



  private def onCreateFleet(msg: CreateFleet): Unit = {
    _fleetComposition = msg.ships
    _dimensions = msg.dimension
    _parent = msg.nextActor // The sender of the message here
  }

  private def onFleetCreated(msg: FleetCreated): Unit = {
    if(msg.fleet.abidesComposition(_fleetComposition, _dimensions) && msg.fleet.isValid){
      // Initialize the actors for the first time
      if(_player1.player == null) _player1 = _player1.playerSet(msg.sender)
      else if(_player2.player == null) _player2 = _player2.playerSet(msg.sender)

      // Set the fleet to the player
      if(_player1 == msg.sender) _player1 = _player1.fleetChanged(msg.fleet)
      else if(_player2 == msg.sender) _player2 = _player2.fleetChanged(msg.fleet)
    }

    if(_player1.isReady && _player2.isReady){
      if(_lastPlayer == _player2){
        _gameEngine ! new StartGame(self, _player1.getPlayerGrid, _player2.getPlayerGrid)
        _lastPlayer = _player1
      } else {
        _gameEngine ! new StartGame(self, _player2.getPlayerGrid, _player1.getPlayerGrid)
        _lastPlayer = _player2
      }
      _player1.player ! new ReadyToStartGame(_gameEngine)
      _player2.player ! new ReadyToStartGame(_gameEngine)
    }
  }

  private def onGameFinished(msg: GameFinished): Unit = {
    // Reset the fleets and increment the scores
    if(_player1 == msg.winner){
      _player1 = _player1.fleetReset.scoreIncremented
      _player2 = _player2.fleetReset
    }
    else {
      _player1 = _player1.fleetReset
      _player2 = _player2.fleetReset.scoreIncremented
    }

    // Print the scores:
    println(Console.BLUE + "\n---------- Round " + (_player1.score + _player2.score) + " ----------" + Console.RESET)
    println("Player 1 (" + _player1.player.path.name + "):\t" +
      _player1.score + (if(_player1 == msg.winner) "\t(winner)" else ""))
    println("Player 2 (" + _player2.player.path.name + "):\t" +
      _player2.score + (if(_player2 == msg.winner) "\t(winner)" else ""))

    // Stop at 100 games
    if(_player1.score + _player2.score == _retries)
      finishActorSystem()
  }

  private def onReplay(msg: Replay): Unit = {
    if(_player1 == msg.sender) _player1 = _player1.replayChanged(msg.replay)
    else if(_player2 == msg.sender)_player2 = _player2.replayChanged(msg.replay)

    if(_player1.wantsToReplay && _player2.wantsToReplay){
      _player1.player ! new CreateFleet(self, _dimensions, _fleetComposition)
      _player2.player ! new CreateFleet(self, _dimensions, _fleetComposition)
      _player1 = _player1.replayReset
      _player2 = _player2.replayReset
    } else if(_player1.wantsToQuit || _player2.wantsToQuit){
      finishActorSystem()
    }
  }

  private def onQuitGame(msg: QuitGame): Unit = {
    finishActorSystem()
  }

  private def finishActorSystem(): Unit = {
    if(_player1.player != null) context stop _player1.player
    if(_player2.player != null) context stop _player2.player
    context stop _gameEngine
    context stop _parent
    context stop self
    System.exit(0)
  }


  private case class Player(player: ActorRef, fleet: FleetGrid, score: Int, _wantsToReplay: Option[Boolean]){

    def getPlayerGrid: (ActorRef, FleetGrid) = (player, fleet)
    def playerSet(actor: ActorRef): Player = Player(actor, fleet, score, _wantsToReplay)
    def fleetChanged(fleetGrid: FleetGrid): Player = Player(player, fleetGrid, score, _wantsToReplay)
    def scoreIncremented: Player = Player(player, fleet, score + 1, _wantsToReplay)
    def replayChanged(replay: Boolean): Player = Player(player, fleet, score, Some(replay))
    def replayReset: Player = Player(player, fleet, score, None)
    def fleetReset: Player = Player(player, null, score, _wantsToReplay)
    def isReady: Boolean = player != null && fleet != null
    def ==(actor: ActorRef): Boolean = player == actor
    def wantsToReplay: Boolean = _wantsToReplay.contains(true)
    def wantsToQuit: Boolean = _wantsToReplay.contains(false)

    def this() = this(null, null, 0, None)

  }

}
