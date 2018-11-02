package core.messages


/**
  * Sent by the players at the end of a game to manage the replays
  *
  * @param playerId The id of the sender of the message
  * @param replay True if the player wants to play again
  */
class Replay(val playerId: String, val replay: Boolean)