package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.operators.MinimizeNotTransported
import no.uib.inf273.operators.MinimizeWaitTime
import no.uib.inf273.operators.MoveSimilarCargo
import no.uib.inf273.operators.given.ReinsertOnceOperator

object SimulatedAnnealingAlgorithmA4 : SimulatedAnnealingAlgorithm(
    0.25 to MinimizeWaitTime,
    0.35 to MinimizeNotTransported,
    0.55 to MoveSimilarCargo,
    fallbackOp = ReinsertOnceOperator(0.75)
) {

    override fun toString(): String {
        return "Simulated Annealing A4"
    }

}
