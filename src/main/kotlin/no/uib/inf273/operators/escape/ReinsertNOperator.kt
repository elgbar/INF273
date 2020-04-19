package no.uib.inf273.operators.escape

import no.uib.inf273.Main
import no.uib.inf273.operators.Operator
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
object ReinsertNOperator : Operator() {

    override fun operate(sol: Solution) {
        //how many cargeos to reinsert
        val n = Main.rand.nextInt(2, min(5, sol.data.nrOfCargo))

        for (i in 0..n) {
            ReinsertOnceOperator.INST.operate(sol)
        }
    }
}
