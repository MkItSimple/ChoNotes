package cho.chonotes.framework.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import cho.chonotes.di.AppComponent
import cho.chonotes.di.DaggerAppComponent
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent(){
        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }


}