package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.operators.Operator.Companion.moveCargo
import no.uib.inf273.processor.Solution

/**
 * Minimize the number of cargoes we use freight to transport
 */
object MinimizeFreight : Operator {
    override val log: Logger = Logger()

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)
        val freights = subs.last()
        if (freights.isEmpty()) return //freight is empty nothing we can do

        val cargo = freights.random(Main.rand) //find a random cargo to transport

        //subtract one from the available vessels to not select the freight array
        val destIndex = Main.rand.nextInt(subs.size - 1)
        val orgIndex = subs.size - 1

        //move the cargo to the new vessel
        moveCargo(sol, subs, orgIndex, destIndex, cargo)

    }
}
