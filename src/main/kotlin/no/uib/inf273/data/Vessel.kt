package no.uib.inf273.data

class Vessel(val id: Int, val homePort: Int, val startTime: Int, val capacity: Int) {

    lateinit var compatibleCalls: IntArray

    /**
     * If this vessel can take given cargo
     */
    fun canTakeCargo(cargo: Int): Boolean {
        return compatibleCalls.contains(cargo)
    }
}
