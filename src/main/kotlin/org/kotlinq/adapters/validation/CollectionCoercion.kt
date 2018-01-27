package org.kotlinq.adapters.validation

import kotlin.reflect.KType


/**
 * Wraps the value in higher dimensions of lists if needed
 */
fun coerceToList(value: List<*>, dimensions: Int): List<*> =
    if (dimensions > 1 && value.firstOrNull()?.let { it::class != List::class } == true) {
      coerceToList(listOf(value), dimensions)
    } else value

