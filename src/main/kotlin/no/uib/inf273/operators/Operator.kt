package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeWithin
import no.uib.inf273.processor.Solution

interface Operator {

    /**
     * Run the operator on the given solution. When returning the solution is guaranteed to be [Solution.isFeasible].
     *
     * @param sol A feasible solution.
     *
     *
     */
    fun operate(sol: Solution)

    companion object {


        internal fun calculateNumberOfVessels(from: Int, until: Int): Int {
            return (until - from + 1) / 2
        }

        /**
         * Find a vessel within a solution that has at least [min] number of vessels within it.
         * If none is found `null` will be returned.
         *
         *
         *
         * @return vessel index, start of vessel array, end of vessel array in that order or `null` if there is no valid vessel
         */
        internal fun findNonEmptyVessel(sol: Solution, min: Int = 2): Triple<Int, Int, Int>? {
            require(min > 0) { "Minimum number of vessels must be > 0, got $min" }
            val barriers: List<Pair<Int, Int>> = sol.getVesselRanges()
            var vIndex: Int
            var from: Int
            var until: Int

            do {
                vIndex = Main.rand.nextInt(barriers.size)
                //two indices where the random value between them will always be within a vessel
                val (t, f) = barriers[vIndex]
                from = t
                until = f

                // Select a subarray that has more than n elements, the number of vessels within a subrange is
                //FIXME an edge-case where every vessel have less than two cargoes, ie if we have more vessels than cargoes
            } while (
                calculateNumberOfVessels(from, until) < min
            )
            return Triple(vIndex, from, until)
        }

        /**
         * Move cargoes around within a vessel in an solution. No change is done to [sol]s [Solution.arr], however [init] will contain the new solution.
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param init The original array of this vessel to use.
         *
         * @return If a feasible solution has been found.
         */
        internal fun operateVesselTilFeasible(
            sol: Solution,
            vIndex: Int,
            init: IntArray,
            operation: (sub: IntArray) -> Unit
        ): Boolean {
            //when empty it is always feasible
            if (init.isEmpty()) return true
            //when there is only one cargo we cannot move it around so we can only return if it is feasible or not
            else if (init.size == 2) {
                return sol.isVesselFeasible(vIndex, init)
            }

            val maxTries = getMaxTries(init)
            var tryNr = 0

            val sub = init.clone()

            do {
                operation(sub)

                if (!init.contentEquals(sub) && sol.isVesselFeasible(vIndex, sub)) {
                    //we found a new feasible solution, update given solution
                    sub.copyInto(init)
                    return true
                } else if (tryNr++ >= maxTries) {
                    //we're out of tries, restore original
                    return false
                } else {
                    //restore sub array to make sure we can only reach direct neighbors
                    init.copyInto(sub)
                }
            } while (true)
        }

        /**
         * Move cargoes around within a vessel in an solution.
         *
         * The operation is to call [IntArray.randomizeWithin] once.
         *
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param init The original array of this vessel to use.
         *
         * @return A feasible solution for the given vessel (if the [init] solution was feasible). This might be identical to the initial solution given if no new solution has been found.
         */
        internal fun exchangeOnceTilFeasible(sol: Solution, vIndex: Int, init: IntArray): Boolean {
            return operateVesselTilFeasible(sol, vIndex, init) {
                it.randomizeWithin()
            }
        }

        /**
         * Calculate how many max tries a vessel should have when exchanging using [operateVesselTilFeasible] or similar methods.
         * It is based on the number of permutation within the array. As this is intended to be used on a vessel range of a solution
         * the size is actually half as large.
         *
         * The number given back is reflect the number of permutation of the array without duplication. Ie an array with the length of four, will
         * have two unique elements (due to how the solution representation is done) called here `a` and `b` all unique permutation of this
         * array is `{aabb, abab, abba, baab, baab, bbaa}` ie a size of six thus given an array of length four the returned value will be six.
         *
         * After seven elements there are more permutations than [Int.MAX_VALUE] so for 8+ elements [Int.MAX_VALUE] is returned
         *
         */
        internal fun getMaxTries(arr: IntArray): Int {
            val size = arr.size
            require(size >= 4 && size % 2 == 0) { "Given size must be even and greater than four. Was given $size" }
            return when (size) {
                4 -> 6                  // 2 elements
                6 -> 90                 // 3 elements
                8 -> 2520               // 4 elements
                10 -> 113_400           // 5 elements
                12 -> 7_484_400         // 6 elements
                14 -> 681_080_400       // 7 elements
                else -> Int.MAX_VALUE   // 8 or more elements, 8 is 81.729.648.000 permutations
            }
        }
    }

    //abc = 4 * -> aabbcc, aabcbc, aacbcb, aaccbb
    //             abbacc,abbcba,abbabc,abcbca,abcacb,ab
}
