package com.siris.messages.extensions

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    var index = 0
    for (item in this) {
        if (predicate(item))
            return index
        index++
    }
    return null
}

fun <T> Collection<T>.toArrayList() = ArrayList(this)
