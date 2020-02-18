package no.uib.inf273.operator

import no.uib.inf273.Main
import no.uib.inf273.extra.exchange
import no.uib.inf273.processor.Solution

object TreeExchangeOperator : Operator {
    override fun operate(sol: Solution) {

        val barriers = sol.getVesselRanges()
        //two indices where the random value between them will always be within a vessel
        val (to, from) = barriers[Main.rand.nextInt(barriers.size - 1)]

        //Cannot randomize an empty array, so we just return
        if (to == from) return

        val indexFirst = Main.rand.nextInt(to, from)
        val indexSecond = Main.rand.nextInt(to, from)
        val indexThird = Main.rand.nextInt(to, from)

        //before the order is first, second, third
        sol.arr.exchange(indexFirst, indexSecond)
        sol.arr.exchange(indexFirst, indexThird)
        //after the order is third, first, second
    }
}
