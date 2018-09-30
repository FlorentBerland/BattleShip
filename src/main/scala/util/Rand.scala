package util

import scala.util.Random

/**
  * This object should be used by the classes which need random generation
  */
object Rand {

  private val _random = new Random()

  def r: Random = _random

}
