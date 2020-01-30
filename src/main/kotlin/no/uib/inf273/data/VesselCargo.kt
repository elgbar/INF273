package no.uib.inf273.data

open class VesselCargo(val originPortTime: Int, val originPortCost: Int, val destPortTime: Int, val destPortCost: Int) {

    /**
     * Object to be used when vessel and cargo is not compatible
     */
    object IncompatibleVesselCargo : VesselCargo(-1, -1, -1, -1) {
    }
}
