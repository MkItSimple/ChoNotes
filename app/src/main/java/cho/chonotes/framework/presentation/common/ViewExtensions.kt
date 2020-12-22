package cho.chonotes.framework.presentation.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import cho.chonotes.business.domain.state.StateMessageCallback
import cho.chonotes.util.TodoCallback

const val COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD = -75

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.fadeIn() {
    val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    apply {
        visible()
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(animationDuration.toLong())
            .setListener(null)
    }
}

fun View.fadeOut(todoCallback: TodoCallback? = null){
    val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    apply {
        animate()
            .alpha(0f)
            .setDuration(animationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    invisible()
                    todoCallback?.execute()
                }
            })
    }
}

fun View.changeColor(newColor: Int) {
    setBackgroundColor(
        ContextCompat.getColor(
            context,
            newColor
        )
    )
}

fun EditText.disableContentInteraction() {
    keyListener = null
    isFocusable = false
    isFocusableInTouchMode = false
    isCursorVisible = false
    setBackgroundResource(android.R.color.transparent)
    clearFocus()
}

fun EditText.enableContentInteraction() {
    keyListener = EditText(context).keyListener
    isFocusable = true
    isFocusableInTouchMode = true
    isCursorVisible = true
    setBackgroundResource(android.R.color.white)
    requestFocus()
    if(text != null){
        setSelection(text.length)
    }
}

fun Activity.displayToast(
    message:String,
    stateMessageCallback: StateMessageCallback
){
    Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    stateMessageCallback.removeMessageFromStack()
}












