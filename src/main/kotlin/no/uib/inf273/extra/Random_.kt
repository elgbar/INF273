package no.uib.inf273.extra

import kotlin.random.Random


fun Random.nextInt0(bound: Int): Int {
    return if (bound != 0) nextInt(bound) else 0
}
