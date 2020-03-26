package no.uib.inf273.data

data class Vessel(val id: Int, val homePort: Int, val startTime: Int, val capacity: Int) {

    lateinit var compatibleCalls: Set<Int>

    fun index(): Int {
        return id - 1
    }

    /**
     * If this vessel can take given cargo
     */
    fun canTakeCargo(cargo: Int): Boolean {
        return compatibleCalls.contains(cargo)
    }
}
