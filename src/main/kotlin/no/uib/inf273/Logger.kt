package no.uib.inf273

object Logger {

    /**
     * How much to log, the higher the number the more will be logged. Log
     */
    var logLevel: Int = 0


    fun log(msg: () -> String) {
        log(msg())
    }

    /**
     * Log something, this will always log
     */
    fun log(msg: String, e: Throwable? = null) {
        println(msg)
        e?.printStackTrace()
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 0`
     */
    fun debug(msg: () -> String) {
        if (logLevel > 0) {
            log(msg(), null)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 0`
     */
    fun debug(e: Throwable? = null, msg: () -> String) {
        if (logLevel > 0) {
            log(msg(), e)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 1`
     */
    fun trace(msg: () -> String) {
        if (logLevel > 1) {
            log(msg(), null)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 1`
     */
    fun trace(e: Throwable? = null, msg: () -> String) {
        if (logLevel > 1) {
            log(msg(), e)
        }
    }


}
