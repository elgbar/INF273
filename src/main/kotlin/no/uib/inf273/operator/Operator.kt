package no.uib.inf273.operator

import no.uib.inf273.Logger.debug
import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.extra.exchange
import no.uib.inf273.extra.filter
import no.uib.inf273.extra.randomizeWithin
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator.Companion.BARRIER_ELEMENT

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

    ReinsertOnceOperator {
        override fun operate(sol: Solution) {
            val sub = sol.splitToSubArray(false)

            //select two vessels where the origin vessel have cargoes
            var orgVesselIndex: Int
            var destVesselIndex: Int
            do {
                orgVesselIndex = rand.nextInt(sub.size)
                destVesselIndex = rand.nextInt(sub.size)
            } while (orgVesselIndex == destVesselIndex || sub[orgVesselIndex].isEmpty())

            //pick a random cargo within the origin vessel
            val cargoIndex = rand.nextInt(sub[orgVesselIndex].size)

            val elem = sub[orgVesselIndex][cargoIndex]

            //remove cargo from the original vessel
            val orgNew = IntArray(sub[orgVesselIndex].size - 2)
            sub[orgVesselIndex].filter(elem, orgNew)
            sub[orgVesselIndex] = orgNew

            //then add both randomly to the new vessel
            val destNew = sub[destVesselIndex].copyOf(sub[destVesselIndex].size + 2)
            destNew[destNew.size - 1] = elem
            destNew[destNew.size - 2] = elem
            sub[destVesselIndex] = destNew

            sol.joinToArray(sub)
        }
    },

    /**
     * An operator capable of move cargoes between vessels
     *
     * It picks two random indices within a solutions array. But only accept those who will somehow change the solution:
     *
     * * The indices cannot be equal
     * * Neither of the picked indices can point to a [BARRIER_ELEMENT]
     *
     * After the two indices are picked there are two paths this operator takes depending whether the picked indices
     * is within the same vessel or not
     *
     * ## Not same vessel
     *
     * In this case the cargo is removed from the origin vessel and placed within the destination vessel.
     * The cargoes are placed last in the destination vessel.
     *
     * ## Same vessel
     *
     * The origin cargo is removed from its position in the array and all elements between origin and destination are
     * shifted to the left or right (depending on whether origin index is less than destination index or visa versa).
     * And the origin cargo is placed at the destination index.
     */
    ReinsertOnceOperatorOld {

        override fun operate(sol: Solution) {
            val ranges = sol.getVesselRanges(false)

            val arr = sol.arr

            //Select two indices that will actually make the solution change in some way.
            // The selected indices are guaranteed to be different and not a barrier element
            var orgIndex: Int
            var destIndex: Int
            do {
                orgIndex = rand.nextInt(arr.size)
                destIndex = rand.nextInt(arr.size)
            } while (orgIndex == destIndex || arr[orgIndex] == BARRIER_ELEMENT || arr[destIndex] == BARRIER_ELEMENT)

            //find the vessel index of origin and destination
            val orgVesselIndex = sol.getVesselIndex(orgIndex, ranges)
            val destVesselIndex = sol.getVesselIndex(destIndex, ranges)

            debug { "Moving element from $orgIndex (vessel $orgVesselIndex) to $destIndex (vessel $destVesselIndex)" }

            if (orgVesselIndex == destVesselIndex) {
                //no point in moving around vessels in the freight sub array
                if (destVesselIndex == ranges.size - 1) {
                    debug { "Vessel of both org and dest is the freight vessel $destVesselIndex" }
                    return
                }

                val elem = arr[orgIndex]

                if (orgIndex < destIndex) {
                    debug { "Same vessel, org less than dest. Moving elements between forwards" }
                    //move elements forwards
                    arr.copyInto(arr, orgIndex, orgIndex + 1, destIndex + 1)
                } else {
                    debug { "Same vessel, org greater than dest. Moving elements between backwards" }
                    //move elements backwards
                    arr.copyInto(arr, destIndex + 1, destIndex, orgIndex)
                }
                arr[destIndex] = elem
            } else {

                debug { "Different vessels, moving all cargoes from org to dest vessel" }

                //TODO make this happen without converting array to list

                val elem = arr[orgIndex]

                val modArr: Array<IntArray> = sol.splitToSubArray(false)

                //remove cargo from the original vessel
                val orgNew = IntArray(modArr[orgVesselIndex].size - 2)
                modArr[orgVesselIndex].filter(elem, orgNew)
                modArr[orgVesselIndex] = orgNew

                //then add both randomly to the new vessel
                val destNew = modArr[destVesselIndex].copyOf(modArr[destVesselIndex].size + 2)
                destNew[destNew.size - 1] = elem
                destNew[destNew.size - 2] = elem
                modArr[destVesselIndex] = destNew

                sol.joinToArray(modArr)
            }

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
}
