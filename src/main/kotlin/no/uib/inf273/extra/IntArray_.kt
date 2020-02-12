package no.uib.inf273.extra

import no.uib.inf273.Logger
import kotlin.random.Random


/**
 * Exchange the elements at the two given indices.
 */
fun IntArray.exchange(first: Int, second: Int) {
    //we don't change the array so do nothing
    if (first == second) {
        Logger.debug { "First is equal to second (both $first), no exchange will happen" }
        return
    }
    //swap the two elements, yes this is kotlin magic
    this[first] = this[second].also { this[second] = this[first] }
}

/**
 * Swap two elements within the given range. Allows [from] to be equal to [until]
 */
fun IntArray.randomizeWithin(from: Int, until: Int, rng: Random = Random.Default) {
    check(from <= until) { "From is strictly greater than until: $from > $until" }

    //Cannot randomize an empty array, so we just return
    if (from == until) {
        Logger.debug { "Range is empty (both $from), no exchange will happen" }
        return
    }

    //generate two indices within the sub-range then swap them
    exchange(rng.nextInt(from, until), rng.nextInt(from, until))
}

/**
 * Filter out the unwanted integer and print collect all remaining elements in [to]
 */
fun IntArray.filter(unwanted: Int, to: IntArray, maxRemoved: Int = Integer.MAX_VALUE) {
    var index = 0
    var removed = 0
    for (e in this) {
        if (e != unwanted) {
            to[index++] = e
            if (++removed == maxRemoved) {
                return
            }
        }
    }
}
