package no.uib.inf273.data

open class VesselCargoCompat(val originTime: Int, val originCost: Int, val destTime: Int, val destCost: Int) {

    /**
     * Object to be used when vessel and cargo is not compatible
     */
    object VesselCargoInCompat : VesselCargoCompat(-1, -1, -1, -1) {

        override fun isCompat(): Boolean {
            return false
        }
    }

    open fun isCompat(): Boolean {
        return true
    }

}
