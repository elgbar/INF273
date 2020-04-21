package no.uib.inf273.extra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Map_test {

    @Test
    fun works() {
        val map = mapOf(1 to 5, 2 to 10).toMutableMap()
        map.mapValuesInPlace { (k, v) -> k * v }
        assertEquals(5, map[1])
        assertEquals(20, map[2])
    }
}
