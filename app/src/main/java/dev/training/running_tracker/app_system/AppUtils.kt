package dev.training.running_tracker.app_system

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object AppUtils {
    fun showSnackBar(view: View, text: String) =
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()

    fun showToast(context: Context, text: String) =
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}