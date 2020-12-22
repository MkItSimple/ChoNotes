package cho.chonotes.framework.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cho.chonotes.R
import cho.chonotes.framework.presentation.auth.AuthActivity
import cho.chonotes.framework.presentation.common.BaseNoteFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
class SplashFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
    }

    private fun checkFirebaseAuth(){
        if(FirebaseAuth.getInstance().currentUser == null){
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        else{
            subscribeObservers()
        }
    }

    private fun subscribeObservers(){
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, Observer { hasSyncBeenExecuted ->
            if(hasSyncBeenExecuted){
                navNoteListFragment()
            }
        })
    }

    private fun navNoteListFragment(){
        findNavController().navigate(R.id.action_splashFragment_to_noteListFragment)
    }

    override fun inject() {
        getAppComponent().inject(this)
    }
}




























