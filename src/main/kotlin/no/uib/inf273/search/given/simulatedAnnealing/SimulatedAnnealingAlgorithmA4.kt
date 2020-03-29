package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.operators.MinimizeNotTransported
import no.uib.inf273.operators.MinimizeWaitTime
import no.uib.inf273.operators.given.ReinsertOnceOperator

object SimulatedAnnealingAlgorithmA4 : SimulatedAnnealingAlgorithm(
    0.30 to MinimizeWaitTime,
    0.50 to MinimizeNotTransported,
//    0.55 to ThreeExchangeOperator,
    fallbackOp = ReinsertOnceOperator(1.0)
) {

    override fun toString(): String {
        return "Simulated Annealing A4"
    }

}
