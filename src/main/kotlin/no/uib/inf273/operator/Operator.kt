package no.uib.inf273.operator

import no.uib.inf273.Logger.debug
import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.processor.Solution
import kotlin.random.Random

enum class Operator {

    /**
     * Swap two elements within a vessel range
     */
    TwoExchangeOperator {
        override fun operate(sol: Solution) {

            val barriers = sol.getVesselRanges()
            //two indices where the random value between them will always be within a vessel
            val (from, until) = barriers[rand.nextInt(barriers.size - 1)]

            sol.arr.randomizeWithin(from, until, rand)
        }
    },

    TreeExchangeOperator {
        override fun operate(sol: Solution) {

            val barriers = sol.getVesselRanges()
            //two indices where the random value between them will always be within a vessel
            val (to, from) = barriers[rand.nextInt(barriers.size - 1)]

            //Cannot randomize an empty array, so we just return
            if (to == from) return

            val indexFirst = rand.nextInt(to, from)
            val indexSecond = rand.nextInt(to, from)
            val indexThird = rand.nextInt(to, from)

            //before the order is first, second, third
            sol.arr.exchange(indexFirst, indexSecond)
            sol.arr.exchange(indexFirst, indexThird)
            //after the order is third, first, second
        }
    },

    /**
     * Pick to indices then move the element from the first element to the second index by shuffling all elements between the two elements
     */
    ReinsertOnceOperator {
        override fun operate(sol: Solution) {
            val arr = sol.arr
            //move from
            val indexOrg = rand.nextInt(arr.size)
            //move to
            val indexDest = rand.nextInt(arr.size)

            //we don't change the solution so do nothing
            if (indexOrg == indexDest) return

            val elem = arr[indexOrg]

            if (indexDest > indexOrg) {
                //move elements forwards
                arr.copyInto(arr, indexOrg, indexOrg + 1, indexDest + 1)
            } else {
                //move elements backwards
                arr.copyInto(arr, indexDest, indexDest - 1, indexOrg - 1)
            }
            
            arr[indexDest] = elem

            //TODO check feasibility
        }
    },

    ;

    /**
     * Run the operation on the given solution.
     *
     * When returning the solution must be [Solution.isFeasible].
     *
     * @param sol A feasible solution
     */
    abstract fun operate(sol: Solution)


    /**
     * Exchange the elements at the two given indices.
     *
     * No range checking is done for speed.
     */
    fun IntArray.exchange(first: Int, second: Int) {
        //we don't change the solution so do nothing
        if (first == second) {
            debug { "First is equal to second (both $first), no exchange will happen" }
            return
        }
        //swap the two elements, yes this is kotlin magic
        this[first] = this[second].also { this[second] = this[first] }
    }

    fun IntArray.randomizeWithin(from: Int, until: Int, rng: Random = Random.Default) {
        check(from <= until) { "From is strictly greater than until: $from > $until" }

        //Cannot randomize an empty array, so we just return
        if (from == until) {
            debug { "Range is empty (both $from), no exchange will happen" }
            return
        }

        //generate two indices within the sub-range then swap them
        exchange(rng.nextInt(from, until), rng.nextInt(from, until))
    }
}
