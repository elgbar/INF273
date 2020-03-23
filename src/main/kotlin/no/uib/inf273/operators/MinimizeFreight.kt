package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.processor.Solution

/**
 * Minimize the number of cargoes we use freight to transport.
 * Cost of not transporting is very high compared to even the worst route!
 * If we minimize number of cargoes we do not transport the cost will (hopefully) go down
 */
object MinimizeFreight : Operator() {

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)
        val freights = subs.last()
        if (freights.isEmpty()) return //freight is empty nothing we can do

        val cargo = freights.random(Main.rand) //find a random cargo to transport

        //subtract one from the available vessels to not select the freight array
        val destIndex = Main.rand.nextInt(subs.size - 1)
        val orgIndex = subs.size - 1 //origin is always dummy vessel

        //move the cargo to the new vessel
        moveCargo(sol, subs, orgIndex, destIndex, cargo)
    }
}
