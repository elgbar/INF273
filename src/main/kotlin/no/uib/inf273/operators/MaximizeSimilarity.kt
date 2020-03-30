package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.processor.Solution

/**
 * Find the vessel with least similarity and try and maximize it.
 *
 * The implementation does not satisfy the quality or speed need from the custom operators.
 * It reasonably quickly find what cargoes are misfit in a vessel but this does not mean that those two are actually
 * a bad solution. Consider the following senario: We have three cargoes A, B, and C.
 * Destination of A is close to origin of B and destination of B is close to origin of C. This might then be an okay solution but for this operator
 * A and C are very dissimilar so it tries to remove one of them, resulting in a worse solution.
 *
 *
 *
 * @author Elg
 */
object MaximizeSimilarity : Operator() {

    init {
        log.logLevel = Logger.TRACE
        log.logLevelLocked = true //force the loglevel to be what it is now
    }

    override fun operate(sol: Solution) {

        val subs = sol.splitToSubArray(true)


        val validVessels = findVessels(sol, subs)

        if (validVessels.isEmpty()) {
            log.debug { "No valid vessels have two or more cargoes" }
            return
        }

        var maxSim = 0.0
        var vIndex = INVALID_VESSEL

        //now find the vessel that is least similar to each other

        for ((i, sub) in validVessels) {
            val similarity = sol.data.getRouteSimilarityScore(i, sub)
            if (maxSim < similarity) {
                maxSim = similarity
                vIndex = i
            }
        }

        //TODO The maximum similarity should not be changed if it is below a certain threshold
        //  But is this value too low? too high? What if all cargoes are very similar? very dissimilar?
        //  A bad solution to this is to remove this check...
        if (maxSim < 0.05) {
            log.debug { "Cargoes in all vessels are so similar for this operator, nothing will be done" }
            return
        }

        val sub = subs[vIndex]

        log.debug {
            "Vessel $vIndex have the least similarity: ${sol.data.getRouteSimilarityScore(vIndex, sub)} | " +
                    "array = ${sub.contentToString()} = ${subs[vIndex].contentToString()}"
        }


        //we have now selected the least similar vessel in the solution

        //but how to make it more similar?
        // remove the one that fit worst in the vessel? where to move that?

        //For now find what caro is least similar

        /**
         * @return Pair of most fitting vessel and the similarity score
         */
        fun findMostFittingVessel(cargo: Int): MutableList<Triple<Int, Int, Double>> {

            val list = ArrayList<Triple<Int, Int, Double>>()

            for ((vi, vSub) in subs.withIndex()) {
                if (!sol.data.canVesselTakeCargo(vi, cargo) || vi == vIndex || sol.data.isDummyVessel(vi)) continue

                val cargoes = vSub.toSet()

                val avg = sol.data.getSimilarityMap(vIndex).filterKeys { (c1, c2) ->
                    cargoes.contains(c1) && cargoes.contains(c2) || c1 == cargo || c2 == cargo
                }.values.average()
                list += Triple(cargo, vi, avg)
            }
            return list
        }

        val map =
            sol.data.getRouteSimilarity(vIndex, subs[vIndex]).toList().sortedByDescending { it.second }.map { it.first }

        outer@
        for ((cargoA, cargoB) in map) {

            log.debug {
                "Checking cargo $cargoA & $cargoB | array = ${sub.contentToString()} = ${subs[vIndex].contentToString()}"
            }

            val a = findMostFittingVessel(cargoA).apply {
                addAll(findMostFittingVessel(cargoB))
                sortBy { it.third }
            }

            for ((cargo, vi, fitness) in a) {

                log.debug {
                    "Maybe move cargo $cargo to $vi | array = ${sub.contentToString()} = ${subs[vIndex].contentToString()}"
                }

                if (fitness >= maxSim) {
                    //the rest of the solutions are worse than this one give up on this iterator
                    break
                }

                if (moveCargo(sol, subs, orgVesselIndex = vIndex, destVesselIndex = vi, cargoId = cargo)) {
                    log.debug { "Successfully moved cargo to $vIndex" }
                    break@outer
                }
            }
        }
    }
}
