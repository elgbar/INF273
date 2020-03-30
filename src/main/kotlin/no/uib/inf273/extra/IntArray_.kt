package no.uib.inf273.extra

import no.uib.inf273.Main
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
fun IntArray.randomizeExchange(from: Int = 0, until: Int = size, rng: Random = Main.rand) {
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

/**
 * Adapted from c-code found [here](https://www.geeksforgeeks.org/print-all-permutations-of-a-string-with-duplicates-allowed-in-input-string/)
 */
fun IntArray.forEachPermutation(copy: Boolean = true, action: IntArray.() -> Unit) {
    val arr = if (copy) this.copyOf() else this

    fun findCeil(arr: IntArray, first: Int, l: Int, h: Int): Int {
        var ceilIndex = l
        for (i in l + 1..h) {
            if (arr[i] > first && arr[i] < arr[ceilIndex]) {
                ceilIndex = i
            }
        }
        return ceilIndex;
    }

//    println("finding all permutations of arr size ${arr.size}: ${arr.contentToString()}")

    // Sort the string in increasing order
    arr.sort()

    // Print permutations one by one
    var isFinished = false;
    while (!isFinished) {
        arr.action()

        var i: Int = size - 2
        while (i >= 0) {
            if (arr[i] < arr[i + 1]) break
            --i
        }

        // If there is no such character, all
        // are sorted in decreasing order,
        // means we just printed the last
        // permutation and we are done.
        if (i == -1)
            isFinished = true;
        else {

            // Find the ceil of 'first char'
            // in right of first character.
            // Ceil of a character is the
            // smallest character greater
            // than it
            val ceilIndex = findCeil(arr, arr[i], i + 1, size - 1);

            // Swap first and second characters
            arr.exchange(i, ceilIndex)


            // Sort the string on right of 'first char'
            arr.sort(fromIndex = i + 1)
        }
    }
}
