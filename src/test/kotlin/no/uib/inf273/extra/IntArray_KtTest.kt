package no.uib.inf273.extra

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class IntArray_KtTest {

    @Test
    internal fun insert_head() {
        val arr = intArrayOf(0, 1, 2, 3)
        arr.insert(0, -1)
        assertArrayEquals(intArrayOf(-1, 0, 1, 2), arr)
    }


    @Test
    internal fun insert_last() {
        val arr = intArrayOf(0, 1, 2, 3)
        arr.insert(3, -1)
        assertArrayEquals(intArrayOf(0, 1, 2, -1), arr)
    }

    @Test
    internal fun insert_middle() {
        val arr = intArrayOf(0, 1, 2, 3)
        arr.insert(2, -1)
        assertArrayEquals(intArrayOf(0, 1, -1, 2), arr)
    }

    @Test
    internal fun permutation() {
        intArrayOf(0, 1, -1, 1).forEachPermutation { println(contentToString()) }
    }
}
