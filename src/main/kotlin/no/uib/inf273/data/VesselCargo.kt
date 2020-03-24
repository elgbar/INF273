package no.uib.inf273.data

data class VesselCargo(val originPortTime: Int, val originPortCost: Int, val destPortTime: Int, val destPortCost: Int) {

    companion object {

        /**
         * Object to be used when vessel and cargo is not compatible
         */
        val incompatibleVesselCargo = VesselCargo(-1, -1, -1, -1)
    }

}
