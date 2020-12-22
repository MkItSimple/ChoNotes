package cho.chonotes.framework.presentation

import cho.chonotes.business.domain.state.DialogInputCaptureCallback
import cho.chonotes.business.domain.state.Response
import cho.chonotes.business.domain.state.StateMessageCallback

interface UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

}


















