package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.operators.MinimizeFreight
import no.uib.inf273.operators.MinimizeWaitTime
import no.uib.inf273.operators.given.ReinsertOnceOperator

object SimulatedAnnealingAlgorithmA4 : SimulatedAnnealingAlgorithm(
    0.30 to MinimizeWaitTime,
    0.50 to MinimizeFreight,
    fallbackOp = ReinsertOnceOperator(0.7)
) {}
