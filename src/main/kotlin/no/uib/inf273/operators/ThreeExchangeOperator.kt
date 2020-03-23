package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.exchange
import no.uib.inf273.processor.Solution

object ThreeExchangeOperator : Operator() {

    override val log = Logger()

    override fun operate(sol: Solution) {
        val (vIndex, from, until) = sol.findNonEmptyVessel(true) ?: return

        val sub = sol.arr.copyOfRange(from, until)

        val feasible = operateVesselTilFeasible(sol, vIndex, sub) {
            val indexFirst = Main.rand.nextInt(it.size)
            val indexSecond = Main.rand.nextInt(it.size)
            var indexThird: Int
            do {
                indexThird = Main.rand.nextInt(it.size)
            } while (sub[indexFirst] == sub[indexThird] && sub[indexSecond] == sub[indexThird])
            //before the order is first, second, third
            it.exchange(indexFirst, indexSecond)
            it.exchange(indexFirst, indexThird)
        }
        if (feasible)
            sub.copyInto(sol.arr, from)
    }
}
