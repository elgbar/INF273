package no.uib.inf273.search.simulatedAnnealing

import no.uib.inf273.operators.ReinsertOnceOperator
import no.uib.inf273.operators.ThreeExchangeOperator
import no.uib.inf273.operators.TwoExchangeOperator

object SimulatedAnnealingSearchA3 : SimulatedAnnealingSearch(
    0.001 to TwoExchangeOperator,
    0.05 to ThreeExchangeOperator,
    fallbackOp = ReinsertOnceOperator
) {}
