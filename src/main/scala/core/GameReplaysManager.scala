package core

import akka.actor.{Actor, ActorRef, Props}
import core.messages._
import core.model.FleetGrid


class GameReplaysManager extends Actor {

  override def receive: Receive = {
    case msg: UseGameConfig => onCreateGameConfig(msg)
    case msg: FleetCreated => onFleetCreated(msg)
    case msg: GameFinished => onGameFinished(msg)
    case msg: Replay => onReplay(msg)
    case msg: QuitGame => onQuitGame(msg)

    case _ =>
  }

  override def postStop(): Unit = {
    context stop _gameEngine
    super.postStop()
  }


  private val _gameEngine: ActorRef = context.actorOf(Props[BattleEngine], "BattleEngine")

  ///// Manager state

  // Values: player1, player2, last starting player
  private var games = Map.empty[UseGameConfig,(Player, Player, ActorRef)]



  private def onCreateGameConfig(msg: UseGameConfig): Unit = {
    // A player should not play in two games at the same time
    if(findGameByPlayer(msg.player1).isEmpty && findGameByPlayer(msg.player2).isEmpty){
      games = games + (msg -> (new Player().playerSet(msg.player1), new Player().playerSet(msg.player2), msg.player1))
      msg.player1 ! new CreateFleet(self, msg.dimensions, msg.ships)
      msg.player2 ! new CreateFleet(self, msg.dimensions, msg.ships)
    }
  }


  private def onFleetCreated(msg: FleetCreated): Unit = {
    val game = findGameByPlayer(msg.sender)
    if(game.isEmpty) return // The actor was not a subscribed player

    // This should shorten the code after...
    val key = game.get._1
    val values = game.get._2

    if(msg.fleet.abidesComposition(key.ships, key.dimensions) && msg.fleet.isValid){
      // Set the fleet to the player
      if(values._1 == msg.sender)
        games = games + (key -> (values._1.fleetChanged(msg.fleet), values._2, values._3))
      else if(values._2 == msg.sender)
        games = games + (key -> (values._1, values._2.fleetChanged(msg.fleet), values._3))
    }

    if(games(key)._1.isReady && games(key)._2.isReady){
      if(games(key)._2 == games(key)._3){
        _gameEngine ! new StartGame(self, games(key)._1.getPlayerGrid, games(key)._2.getPlayerGrid)
        games = games + (key -> (games(key)._1, games(key)._2, games(key)._1.player))
      } else {
        _gameEngine ! new StartGame(self, games(key)._2.getPlayerGrid, games(key)._1.getPlayerGrid)
        games = games + (key -> (games(key)._1, games(key)._2, games(key)._2.player))
      }
    }
  }

  private def onGameFinished(msg: GameFinished): Unit = {
    if(msg.sender != _gameEngine) return
    val game = findGameByPlayer(msg.winner)
    if(game.isEmpty) return // Should not happen

    val key = game.get._1
    val values = game.get._2

    // Reset the fleets and increment the scores
    if(values._1 == msg.winner)
      games = games + (key -> (values._1.fleetReset.scoreIncremented, values._2.fleetReset, values._3))
    else
      games = games + (key -> (values._1.fleetReset, values._2.fleetReset.scoreIncremented, values._3))

    // Print the scores:
    println(Console.BLUE + "\n---------- Round " +
      (games(key)._1.score + games(key)._2.score) +
      " ----------" + Console.RESET)
    println("Player 1 (" + values._1.player.path.name + "):\t" +
      games(key)._1.score + (if(games(key)._1 == msg.winner) "\t(winner)" else ""))
    println("Player 2 (" + values._2.player.path.name + "):\t" +
      games(key)._2.score + (if(games(key)._2 == msg.winner) "\t(winner)" else ""))

    // Max number of rounds reached
    if(games(key)._1.score + games(key)._2.score == key.replays)
      finishGame(key)
  }

  private def onReplay(msg: Replay): Unit = {
    val game = findGameByPlayer(msg.sender)
    if(game.isEmpty) return

    val key = game.get._1
    val values = game.get._2

    if(values._1 == msg.sender)
      games = games + (key -> (values._1.replayChanged(msg.replay), values._2, values._3))
    else if(values._2 == msg.sender)
      games = games + (key -> (values._1, values._2.replayChanged(msg.replay), values._3))

    if(games(key)._1.wantsToReplay && games(key)._2.wantsToReplay){
      games(key)._1.player ! new CreateFleet(self, key.dimensions, key.ships)
      games(key)._2.player ! new CreateFleet(self, key.dimensions, key.ships)
      games = games + (key -> (games(key)._1.replayReset, games(key)._2.replayReset, games(key)._3))
    } else if(games(key)._1.wantsToQuit || games(key)._2.wantsToQuit){
      finishGame(key)
    }
  }

  private def onQuitGame(msg: QuitGame): Unit = {
    val game = findGameByPlayer(msg.sender)
    game.foreach(g => finishGame(g._1))
  }


  /**
    * Retrieves the game that is played by the given player actor
    *
    * @param player The player to search
    * @return The game key-values pair found if it exists
   */
  private def findGameByPlayer(player: ActorRef): Option[(UseGameConfig, (Player, Player, ActorRef))] =
    games.find(g => g._2._1 == player || g._2._2 == player)


  /**
    * Terminates a game, and remove its config
    *
    * @param config The game config to terminate
    */
  private def finishGame(config: UseGameConfig): Unit = {
    games.get(config).foreach(g => {
      if(config.sender == null) {
        context stop config.player1
        context stop config.player2
      } else {
        config.sender ! new EndOfMatch(self, config.player1, games(config)._1.score, config.player2, games(config)._2.score)
      }
    })
    games = games - config
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
