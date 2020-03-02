package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.processor.Solution

/**
 * An intensification operator that works within cargoes to handle the most urgent cargoes first.
 * TODO find out if we maybe should rather try to minimize wait time (for port opening) instead?
 */
object UrgentFirst : Operator {
    override val log: Logger = Logger()

    override fun operate(sol: Solution) {
        
    }
}