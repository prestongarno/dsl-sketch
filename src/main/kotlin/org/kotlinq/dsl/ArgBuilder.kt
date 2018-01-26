package org.kotlinq.dsl


class ArgBuilder {
  private val args = mutableMapOf<String, Any>()

  fun take(argument: Pair<String, Any>) {
    args[argument.first] = argument.second
  }
}