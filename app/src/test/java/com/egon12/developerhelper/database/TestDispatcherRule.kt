package com.egon12.developerhelper.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@ExperimentalCoroutinesApi
class TestDispatcherRule : TestRule {

    val dispatcher = TestCoroutineDispatcher()

    override fun apply(base: Statement?, description: Description?): Statement {
        return SandwichStatement(base)
    }

    fun before() {
        // Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
        // All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
        Dispatchers.setMain(dispatcher)
    }

    fun after() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        dispatcher.cleanupTestCoroutines()
    }

    /**
     * Statement that call before and after test is executed
     */
    private inner class SandwichStatement(val base: Statement?) : Statement() {
        override fun evaluate() {
            before()
            base?.evaluate()
            after()
        }
    }

}

