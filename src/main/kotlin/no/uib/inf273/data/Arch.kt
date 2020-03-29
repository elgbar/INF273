package no.uib.inf273.data

/**
 * Vessel arc that a node can take
 */
data class Arch(val vessel: Int, val ori: Int, val dest: Int, val time: Int, val cost: Int) {}
