package com.ownboard.app

class ValueListener<T : Any>(initialValue: T) {

    private var _value: T = initialValue
    
    private val listeners = mutableListOf<(T) -> Unit>()

    var value: T
        get() = _value
        set(newValue) {
            // التحقق من التغيير قبل التحديث
            if (_value != newValue) {
                _value = newValue
                notifyListeners(newValue)
            }
        }

    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (T) -> Unit) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    // إشعار المستمعين بالقيمة الحالية
    fun notifyListeners() {
        notifyListeners(_value)
    }

    // دالة خاصة للإشعار
    private fun notifyListeners(value: T) {
        listeners.forEach { it.invoke(value) }
    }
}