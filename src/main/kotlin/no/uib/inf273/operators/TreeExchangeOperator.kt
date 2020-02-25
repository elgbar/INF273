package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.exchange
import no.uib.inf273.operators.Operator.Companion.operateVesselTilFeasible
import no.uib.inf273.processor.Solution

object TreeExchangeOperator : Operator {

    override fun operate(sol: Solution) {
        val (vIndex, from, until) = Operator.findNonEmptyVessel(sol) ?: return

        val sub = sol.arr.copyOfRange(from, until)

        val feasible = operateVesselTilFeasible(sol, vIndex, sub) {
            val indexFirst = Main.rand.nextInt(it.size)
            val indexSecond = Main.rand.nextInt(it.size)
            val indexThird = Main.rand.nextInt(it.size)

            //before the order is first, second, third
            it.exchange(indexFirst, indexSecond)
            it.exchange(indexFirst, indexThird)
        }
        if (feasible)
            sub.copyInto(sol.arr, from)
    }
}
