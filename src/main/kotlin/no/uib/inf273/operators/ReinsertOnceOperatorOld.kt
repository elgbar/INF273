package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.filter
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.processor.SolutionGenerator.Companion.BARRIER_ELEMENT


/**
 * An operator capable of move cargoes between vessels (-ish, see `issues` section).
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
 *
 * ## Issues
 *
 * This operator is not capable of moving cargoes to empty vessels (such as in the case of [SolutionGenerator]).
 */
object ReinsertOnceOperatorOld : Operator {

    override fun operate(sol: Solution) {
        val ranges = sol.getVesselRanges(false)

        val arr = sol.arr

        //Select two indices that will actually make the solution change in some way.
        // The selected indices are guaranteed to be different and not a barrier element
        var orgIndex: Int
        var destIndex: Int
        do {
            orgIndex = Main.rand.nextInt(arr.size)
            destIndex = Main.rand.nextInt(arr.size)
        } while (orgIndex == destIndex || arr[orgIndex] == SolutionGenerator.BARRIER_ELEMENT || arr[destIndex] == SolutionGenerator.BARRIER_ELEMENT)

        //find the vessel index of origin and destination
        val orgVesselIndex = sol.getVesselIndex(orgIndex, ranges)
        val destVesselIndex = sol.getVesselIndex(destIndex, ranges)

        Logger.debug { "Moving element from $orgIndex (vessel $orgVesselIndex) to $destIndex (vessel $destVesselIndex)" }

        if (orgVesselIndex == destVesselIndex) {
            //no point in moving around vessels in the freight sub array
            if (destVesselIndex == ranges.size - 1) {
                Logger.debug { "Vessel of both org and dest is the freight vessel $destVesselIndex" }
                return
            }

            val elem = arr[orgIndex]

            if (orgIndex < destIndex) {
                Logger.debug { "Same vessel, org less than dest. Moving elements between forwards" }
                //move elements forwards
                arr.copyInto(arr, orgIndex, orgIndex + 1, destIndex + 1)
            } else {
                Logger.debug { "Same vessel, org greater than dest. Moving elements between backwards" }
                //move elements backwards
                arr.copyInto(arr, destIndex + 1, destIndex, orgIndex)
            }
            arr[destIndex] = elem
        } else {

            Logger.debug { "Different vessels, moving all cargoes from org to dest vessel" }

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
        //TODO Ensure the solution is feasible
    }
}
