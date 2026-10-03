package com.winter.core

class ObservableList<T>(
    private val list: MutableList<T> = mutableListOf()
): MutableList<T> by list {

    companion object {
        @JvmStatic
        fun emptyObservableList() {
            return ObservableList
        }
    }
    private object EmptyObservableList
    private var index = 0

    override fun get(index: Int): T{
        this.index = index
        return list.get(index)
    }

    fun getRelativeIndex(index: Int): T{
        return list.get(this.index + index)
    }
}