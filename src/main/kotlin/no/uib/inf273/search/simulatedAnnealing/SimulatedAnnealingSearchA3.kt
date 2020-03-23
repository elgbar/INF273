package no.uib.inf273.search.simulatedAnnealing

import no.uib.inf273.operators.given.ReinsertOnceOperator
import no.uib.inf273.operators.given.ThreeExchangeOperator
import no.uib.inf273.operators.given.TwoExchangeOperator

object SimulatedAnnealingSearchA3 : SimulatedAnnealingSearch(
    0.001 to TwoExchangeOperator,
    0.05 to ThreeExchangeOperator,
    fallbackOp = ReinsertOnceOperator
) {}
