package no.uib.inf273.operators.escape

import no.uib.inf273.Main
import no.uib.inf273.operators.given.ReinsertOnceOperator
import no.uib.inf273.processor.Solution
import kotlin.math.min

/**
 * Move lots of cargoes around.
 *
 * This is an escape operator.
 *
 * @author Elg
 */
object ReinsertNOperator : EscapeOperator() {

    private val r1 = ReinsertOnceOperator(maxCargoesToBruteForce = 0)

    override fun escape(sol: Solution) {
        //how many cargeos to reinsert
        val n = Main.rand.nextInt(2, min(10, sol.data.nrOfCargo))

        for (i in 0..n) {
            r1.operate(sol)
        }
    }
}
