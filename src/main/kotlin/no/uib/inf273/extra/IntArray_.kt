package no.uib.inf273.extra

import kotlin.random.Random


/**
 * Exchange the elements at the two given indices.
 */
fun IntArray.exchange(first: Int, second: Int) {
    //we don't change the array so do nothing
    if (first != second) {
        //swap the two elements, yes this is kotlin magic
        this[first] = this[second].also { this[second] = this[first] }
    }
}

/**
 * Swap two elements within the given range. Allows [from] to be equal to [until]
 */
fun IntArray.randomizeExchange(from: Int = 0, until: Int = size, rng: Random = Random.Default) {
    check(from <= until) { "From is strictly greater than until: $from > $until" }

    //Cannot randomize an empty array, so we just return
    if (from != until) {

        //generate two indices within the sub-range then swap them
        exchange(rng.nextInt(from, until), rng.nextInt(from, until))
    }
}


/**
 * Remove exactly [amount] from this array
 */
fun IntArray.filter(unwanted: Int, amount: Int): IntArray {
    return filter(unwanted, IntArray(size - amount), amount)
}

/**
 * Filter out the unwanted integer and print collect all remaining elements in [to]
 *
 * @return [to]
 */
fun IntArray.filter(unwanted: Int, to: IntArray, maxRemoved: Int = Integer.MAX_VALUE): IntArray {
    var index = 0
    var removed = 0
    for (e in this) {
        if (e != unwanted) {
            to[index++] = e
            if (++removed == maxRemoved) {
                return to
            }
        }
    }
    return to
}

/**
 * Insert [element] at [index], pushing everything right of [index] to the right. The element at [IntArray.size]` - 1` will be removed
 */
fun IntArray.insert(index: Int, element: Int) {
    this.copyInto(this, index + 1, index, size - 1)
    this[index] = element
}
