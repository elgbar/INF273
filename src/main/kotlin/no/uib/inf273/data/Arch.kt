package no.uib.inf273.data

/**
 * Vessel arc that a node can take
 */
data class Arch(val vessel: Int, val ori: Int, val dest: Int, val time: Int, val cost: Int) {

    override fun toString(): String {
        return "Arch(vessel=$vessel, ori=$ori, dest=$dest, time=$time, cost=$cost)"
    }
}
