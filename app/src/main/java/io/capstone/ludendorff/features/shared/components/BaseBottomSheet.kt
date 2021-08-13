package io.capstone.ludendorff.features.shared.components

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.capstone.ludendorff.R

abstract class BaseBottomSheet(private val manager: FragmentManager)
    : BottomSheetDialogFragment() {

    protected fun createToast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(requireContext(), id, duration).apply { show() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            with(BottomSheetBehavior.from(bottomSheet)) {
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    fun show() {
        if (!this.isAdded || !this.isVisible)
            show(manager, this::class.simpleName)
    }

    inline fun show(sheet: BaseBottomSheet.() -> Unit) {
        this.sheet()
        this.show()
    }

    protected fun hideViews(vararg views: View) {
        views.forEach { it.isVisible = false }  
    }
}