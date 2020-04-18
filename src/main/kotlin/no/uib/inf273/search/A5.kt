package no.uib.inf273.search

import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator

/**
 * An algorithm based an Simulated Annealing, Tabu, and performance of operators
 *
 * ## Description of the algorithm
 *
 * A very high level pseudocode of the algorithm is as follows
 *
 * ```
 * S -> Set of operators
 * W -> Weight of operator (in same iteration order as S)
 * I -> number of iterations
 * B -> Global best solution
 * C -> Current solution
 * T -> Current temperature
 * L -> List of taboo results
 *
 * for iter_nr in 0..I:
 *     if iter_nr mod (1% of I) is 0:
 *         Recalculating the operator weights and reset all point counters
 *     O <- Select operator based on weights W
 *     N <- Operate on C with selected operator O
 *     ∆E <- objective value of N - objective value of B
 *
 *     if N is feasible:
 *         # This part is simulated annealing with taboo check
 *         if ∆E < 0 and N is not in L:
 *             Set C to be N and update L
 *             if objective value of N < objective value of B
 *                 Set B to be N
 *         else if rand(0d..1d) < e ^ (-∆E / T):
 *             Set C to be N and update L
 *
 *     Calculate points to give to O based on N
 *     Update T
 * ```
 *
 * ## Automatic weighing of the operators
 *
 * The given operators used are automatically weighted based on their performance in the last iteration segment.
 *
 * ### Iteration Segment
 *
 * An iteration segment is defined as lasting for 1% of the total iterations.
 * In other words there are in total 100 iteration segments each search.
 *
 * The reason to use a percentage and not a fixed number to let the algorithm scale to the number of iterations.
 * Having more iterations to gather the weights to be used is seen as a positive element.
 * It also allow the algorithm to scale down the number of iterations if a quick search is needed for what ever reason.
 *
 * ### Operator Scoring System
 *
 * For all conditions below change the score accordingly to their weight.
 *
 * * Global best solution       += 2.50 pt (Greater weight if a new global best is found to encourage this)
 * * Better solution            += 0.50 pt
 * * Identical solution         += 0.50 pt (No change have was made)
 * * Feasible solution          += 0.50
 * * Worse  solution            += 0.25 pt (At least it is feasible)
 * * Taboo solution             -= 0.50 pt (Discourage taboo solutions, note that C does not count as taboo)
 * * Worse infeasible solution  -= 0.25 pt
 *
 *  ### Calculating the new Weights
 *
 *  Each operator have a unbiased weight score `u` equal to `max(0, total points scored in the last segment)` divided
 *  by the total time the operator was selected. The old weight from the previous segment is called `o`, `n` is the number of operators.
 *
 *  The new relative weight `r` is `(o/2 + u) / n`.
 *
 * ## Taboo Solutions
 *
 * There should be a small cache that disallows recently seen solution (like in Tabu search) use the hashed value of the solution array.
 * The cache should be a double linked set of determined size. When a new (non-taboo) solution is should the hash of this solution
 * is added to the front of the list and the tail of the list is removed, only if the size of the list is greater than the
 * determined size. If the determined size of the list is changed to a smaller size the tail of the list is removed.
 *
 * ### Taboo Size
 *
 * The size of the taboo list should be dynamic should be at least `max(1, 0.001% of total iterations)` and less than 0.01% of total iterations.
 * The size should normally be at max length, as specified above, but should be shortened when there are no new solutions found for a while.
 *
 * ### Hash Collisions
 *
 * There should not be any hash duplicates in the taboo set. If a duplicate is being added it should be added in
 * such a way that it is the youngest (ie first) member of the set. This can be done by removing then adding the element.
 * It might be added because two different solutions gives the same hash or it might be added due to the boltzmann probability.
 *
 *
 * @author Elg
 */
object A5 : Algorithm() {
    override fun search(sol: Solution, iterations: Int): Solution {
        TODO("not implemented")
    }

    override fun tune(solgen: SolutionGenerator, iterations: Int, report: Boolean) {
        TODO("not implemented")
    }

}
