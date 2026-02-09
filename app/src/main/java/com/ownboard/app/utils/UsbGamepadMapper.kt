package com.ownboard.app

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.util.Log

class UsbGamepadMapper(private var inputConnection: InputConnection?) {

    /**
     * الخريطة محدثة بناءً على اختبارك:
     * المفتاح (يسار): الكود القادم من يدك (188, 189...)
     * القيمة (يمين): زر اليد المعياري (A, B, X...)
     */
    private val keyMapping = mapOf(
        // المجموعة الأولى (الأزرار الأساسية)
        188 to KeyEvent.KEYCODE_BUTTON_A,      // (كان 1) -> A
        189 to KeyEvent.KEYCODE_BUTTON_B,      // (كان 2) -> B
        190 to KeyEvent.KEYCODE_BUTTON_Y,      // (كان 3) -> Y
        191 to KeyEvent.KEYCODE_BUTTON_X,      // (كان 4) -> X

        // المجموعة الثانية (الأزرار الخلفية L/R)
        192 to KeyEvent.KEYCODE_BUTTON_L1,     // (كان 5) -> L1
        193 to KeyEvent.KEYCODE_BUTTON_R1,     // (كان 6) -> R1
        194 to KeyEvent.KEYCODE_BUTTON_L2,     // (كان 7) -> L2
        195 to KeyEvent.KEYCODE_BUTTON_R2,     // (كان 8) -> R2

        // المجموعة الثالثة (أزرار التحكم)
        196 to KeyEvent.KEYCODE_BUTTON_SELECT, // (كان 9) -> Select
        197 to KeyEvent.KEYCODE_BUTTON_START   // (كان 10) -> Start
    )

    fun processKey(event: KeyEvent): Boolean {
        // نتجاهل الكود 23 لأنه مجرد تشويش، ونبحث عن الكود الحقيقي
        if (event.keyCode == 23) return false 

        // نستخدم keyCode لأنه كان واضحاً في تجربتك (188, 189...)
        // وإذا لم نجده، نجرب scanCode كاحتياط
        val targetKeyCode = keyMapping[event.keyCode] ?: keyMapping[event.scanCode]

        if (targetKeyCode != null) {
            val newEvent = KeyEvent(
                event.downTime,
                event.eventTime,
                event.action, // ننقل الحالة (ضغط أو رفع) كما هي
                targetKeyCode,
                event.repeatCount,
                event.metaState,
                event.deviceId,
                event.scanCode,
                event.flags,
                event.source
            )

            try {
                inputConnection?.sendKeyEvent(newEvent)
                Log.d("GamePad", "تم تحويل ${event.keyCode} إلى $targetKeyCode")
            } catch (e: Exception) {
                Log.e("GamePad", "فشل الإرسال", e)
            }
            return true
        }
        return false
    }

    fun setConnection(conn: InputConnection?) {
        this.inputConnection = conn
    }
}