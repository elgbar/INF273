package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.Main
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * @author Elg
 */
internal class DataParserTest {


    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            Solution.log.logLevel = Logger.DEBUG
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }

    @Test
    internal fun mostSimilar() {
        val vessel = data.vessels[0]
        val cargoA = 1

        assertTrue(vessel.canTakeCargo(cargoA)) { "Vessel $vessel Can't take cargo $cargoA" }
        assertEquals(0.0, data.getSimilarity(0, cargoA, cargoA))
    }

    @Test
    internal fun swappedCargoIsSame() {
        val vessel = data.vessels[0]

        val cargoA = 1
        val cargoB = 3

        assertTrue(vessel.canTakeCargo(cargoA)) { "Vessel $vessel Can't take cargo $cargoA" }
        assertTrue(vessel.canTakeCargo(cargoB)) { "Vessel $vessel Can't take cargo $cargoB" }
        
        assertEquals(data.getSimilarity(0, cargoA, cargoB), data.getSimilarity(0, cargoB, cargoA))
    }
}
