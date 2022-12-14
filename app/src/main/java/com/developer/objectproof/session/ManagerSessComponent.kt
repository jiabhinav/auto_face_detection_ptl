package com.developer.objectproof.session
import android.content.Context
import com.developer.objectproof.views.CameraActivity
import com.developer.objectproof.views.InstructionActivity
import com.developer.objectproof.views.RegisterActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class MainActivityModule(context: Context) {
    private val context: Context
    @Provides //scope is not necessary for parameters stored within the module
    fun context(): Context {
        return context
    }
    init {
        this.context = context
    }
}

@Component(modules = [MainActivityModule::class])
@Singleton
interface ManagerSessComponent {
    fun context(): Context?
    fun inject(activity: RegisterActivity?)
    fun inject(activity: CameraActivity?)
    fun inject(activity: InstructionActivity?)

}
