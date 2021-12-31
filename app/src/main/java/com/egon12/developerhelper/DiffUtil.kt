package com.egon12.developerhelper

import androidx.recyclerview.widget.DiffUtil
import kotlin.reflect.KClass

class UuidDiffUtil<T : HasUUID>(@Suppress("UNUSED_PARAMETER") o: KClass<T>) :
    DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem.uuid == newItem.uuid

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}