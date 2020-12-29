package com.egon12.developerhelper

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.hilt.lifecycle.ViewModelAssistedFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
































import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionFragmentTest {

    /*
    @Test
    fun testInit() {
        val scenario = launchFragmentInContainer<ConnectionFragment>()
        scenario.moveToState(Lifecycle.State.CREATED)
        Thread.sleep(1000)
    }
     */
}

/*
@HiltAndroidTest
@UninstallModules(DatabaseViewModel_HiltModule::class)
class ConnectionFragmentTest {

    @Module
    @InstallIn(ActivityRetainedComponent::class)
    class TestModule : ViewModelAssistedFactory<DatabaseViewModel> {
        @Provides
        @IntoMap
        @StringKey("com.egon12.developerhelper.DatabaseViewModel")
        override fun create(handle: SavedStateHandle) = mock<DatabaseViewModel>()
    }


    /*
    @BindValueIntoMap
    @JvmField
    @StringKey("com.egon12.developerhelper.DatabaseViewModel")
    val viewModelFactory: ViewModelAssistedFactory<out ViewModel> = this

    override fun create(handle: SavedStateHandle): DatabaseViewModel = model

     */


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    //@get:Rule
    //var rule = activityScenarioRule<MainActivity>()

    @Test
    fun testInit() {
        //hiltRule.inject()
        Thread.sleep(1000)
    }

}

 */