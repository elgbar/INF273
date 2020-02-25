package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.processor.DataParser

internal class ReinsertOnceOperatorTest {

    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            ReinsertOnceOperator.log.logLevel = Logger.TRACE
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }

}
