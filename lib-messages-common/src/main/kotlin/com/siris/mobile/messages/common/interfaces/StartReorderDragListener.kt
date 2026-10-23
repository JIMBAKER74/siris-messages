package com.siris.mobile.messages.common.interfaces

import androidx.recyclerview.widget.RecyclerView

interface StartReorderDragListener {
    fun requestDrag(viewHolder: RecyclerView.ViewHolder)
}
