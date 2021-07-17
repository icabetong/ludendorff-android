package io.capstone.keeper.components.interfaces

interface OnItemActionListener<T> {
    fun onActionPerformed(data: T?, action: Action)

    enum class Action { SELECT, DELETE }
}