package no.uib.inf273

object Logger {


    const val TRACE = 2
    const val DEBUG = 1
    const val INFO = 0
    const val NONE = -1

    /**
     * How much to log, the higher the number the more will be logged. Log
     */
    var logLevel: Int = INFO


    fun log(msg: () -> String) {
        if (logLevel >= INFO) {
            log(msg(), null)
        }
    }

    /**
     * Log something, this will always log
     */
    fun log(msg: String, e: Throwable? = null) {
        if (logLevel <= NONE) return
        println(msg)
        e?.printStackTrace()
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 0`
     */
    fun debug(msg: () -> String) {
        if (logLevel >= DEBUG) {
            log("[DBG] " + msg(), null)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 0`
     */
    fun debugs(msgs: () -> List<String>) {
        if (logLevel >= DEBUG) {
            for (msg in msgs()) {
                log("[DBG] $msg", null)
            }
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 0`
     */
    fun debug(e: Throwable? = null, msg: () -> String) {
        if (logLevel >= DEBUG) {
            log("[DBG] " + msg(), e)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 1`
     */
    fun trace(msg: () -> String) {
        if (logLevel >= TRACE) {
            log("[TRC] " + msg(), null)
        }
    }

    /**
     * Log a message lazily, the string will not be computed if debug is disabled
     *
     * Will only log if `logLevel > 1`
     */
    fun trace(e: Throwable? = null, msg: () -> String) {
        if (logLevel >= TRACE) {
            log("[TRC] " + msg(), e)
        }
    }
}
