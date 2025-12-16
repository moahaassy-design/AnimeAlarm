package com.anime.alarm.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmChallengeTest {

    @Test
    fun testShakeChallengeCreation() {
        val challenge = AlarmChallenge.ShakeChallenge(20)
        assertEquals(20, challenge.shakesRequired)
        assertTrue(challenge is AlarmChallenge)
    }

    @Test
    fun testMathChallengeCreation() {
        val challenge = AlarmChallenge.MathChallenge(MathDifficulty.HARD)
        assertEquals(MathDifficulty.HARD, challenge.difficulty)
    }

    @Test
    fun testMemoryChallengeCreation() {
        val challenge = AlarmChallenge.MemoryChallenge(5)
        assertEquals(5, challenge.numRounds)
    }

    @Test
    fun testNoneChallengeCreation() {
        val challenge = AlarmChallenge.None
        assertTrue(challenge is AlarmChallenge.None)
    }
}
