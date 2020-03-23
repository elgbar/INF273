package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.operators.MinimizeFreight
import no.uib.inf273.operators.MinimizeWaitTime
import no.uib.inf273.operators.UrgentFirst

object SimulatedAnnealingAlgorithmA4 : SimulatedAnnealingAlgorithm(
    0.0 to MinimizeWaitTime,
    0.999999 to MinimizeFreight,
    fallbackOp = UrgentFirst
) {}
