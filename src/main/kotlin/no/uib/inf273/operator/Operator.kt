package no.uib.inf273.operator

import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.processor.Solution

enum class Operator {

    TwoExchangeOperator {
        override fun operate(sol: Solution) {

            val indexOrigin = rand.nextInt(sol.arr.size)
            val indexDest = rand.nextInt(sol.arr.size)

            sol.arr.exchange(indexOrigin, indexDest)
        }
    },

    TreeExchangeOperator {
        override fun operate(sol: Solution) {
            val indexFirst = rand.nextInt(sol.arr.size)
            val indexSecond = rand.nextInt(sol.arr.size)
            val indexThird = rand.nextInt(sol.arr.size)

            //before the order is first, second, third
            sol.arr.exchange(indexFirst, indexSecond)
            sol.arr.exchange(indexFirst, indexThird)
            //after the order is third, first, second
        }
    },

    /**
     * Pick to indexes then move the element from the first element to the second index by shuffling all elements between the two elements
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
                arr.copyInto(arr, indexDest, indexDest + 1, indexOrg)
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
     * Exchange the elements at the two given indexes.
     *
     * No range checking is done for speed.
     */
    fun IntArray.exchange(first: Int, second: Int) {
        //we don't change the solution so do nothing
        if (first == second) return
        //swap the two elements, yes this is kotlin magic
        this[first] = this[second].also { this[second] = this[first] }
    }
}
