package no.uib.inf273.operator

import no.uib.inf273.processor.Solution
import kotlin.random.Random

enum class Operator {

    TwoExchangeOperator {
        override fun operate(sol: Solution) {

            val indexOrigin = rand.nextInt(sol.solArr.size)
            val indexDest = rand.nextInt(sol.solArr.size)

            //we don't change the solution so do nothing
            if (indexOrigin == indexDest) return

            //swap the two elements, yes this is kotlin magic
            sol.solArr[indexOrigin] = sol.solArr[indexDest].also { sol.solArr[indexDest] = sol.solArr[indexOrigin] }
        }
    },

    TreeExchangeOperator {
        override fun operate(sol: Solution) {
            TODO("not implemented")
        }
    },

    /**
     * Pick to indexes then move the element from the first element to the second index by shuffling all elements between the two elements
     */
    ReinsertOnceOperator {
        override fun operate(sol: Solution) {
            val arr = sol.solArr
            //move from
            val indexOrg = rand.nextInt(arr.size)
            //move to
            val indexDest = rand.nextInt(arr.size)

            //we don't change the solution so do nothing
            if (indexOrg == indexDest) return

            val elem = arr[indexOrg]

            if (indexDest > indexOrg) {
                //move elements backwards
                arr.copyInto(arr, indexDest + 1, indexDest + 1, indexOrg + 1)
            } else {
                //move elements forwards
                arr.copyInto(arr, indexOrg, indexOrg, indexDest)
            }
            arr[indexDest] = elem

            //TODO check feasibility
        }
    };


    /**
     * Random to be used within the operator
     */
    val rand: Random
        get() = Random.Default

    /**
     * Run the operation on the given solution.
     *
     * When returning the solution must be [Solution.isFeasible].
     *
     * @param sol A feasible solution
     */
    abstract fun operate(sol: Solution)

}
