package io.capstone.ludendorff.components.interfaces

import android.view.View

interface OnItemActionListener<T> {
    fun onActionPerformed(data: T?, action: Action, container: View?)

    enum class Action { SELECT, DELETE }
}