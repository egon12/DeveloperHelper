package com.egon12.developerhelper.database.fragment


import androidx.test.ext.junit.runners.AndroidJUnit4
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
        @StringKey("com.egon12.developerhelper.database.viewmodel.DatabaseViewModel")
        override fun create(handle: SavedStateHandle) = mock<DatabaseViewModel>()
    }


    /*
    @BindValueIntoMap
    @JvmField
    @StringKey("com.egon12.developerhelper.database.viewmodel.DatabaseViewModel")
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