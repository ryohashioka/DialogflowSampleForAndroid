package jp.wiseplants.googleassistanttest.activity.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

/**
 * A simple dialog with a message.
 *
 *
 * The calling [android.app.Activity] needs to implement [ ].
 */
class MessageDialogFragment : AppCompatDialogFragment() {

    interface Listener {
        /**
         * Called when the dialog is dismissed.
         */
        fun onMessageDialogDismissed()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setMessage(arguments!!.getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { dialog, which -> (activity as Listener).onMessageDialogDismissed() }
                .setOnDismissListener { (activity as Listener).onMessageDialogDismissed() }
                .create()
    }

    companion object {

        private val ARG_MESSAGE = "message"

        /**
         * Creates a new instance of [MessageDialogFragment].
         *
         * @param message The message to be shown on the dialog.
         * @return A newly created dialog fragment.
         */
        fun newInstance(message: String): MessageDialogFragment {
            val fragment = MessageDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

}
