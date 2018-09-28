package core.messages

import akka.actor.Props

/**
  * Should be sent to the GameConfigManager to initialize the game
  *
  * @param firstPlayerProps The first player to create, or nothing to wait for the first player
  */
class InitGame(val firstPlayerProps: Props)
