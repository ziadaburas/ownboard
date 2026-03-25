# Project Problems Report

## Performance Issues

### Keyboard Unexpectedly Closes

**Description:** The keyboard application sometimes closes unexpectedly and abruptly. This issue points to potential underlying performance or stability problems within the application.

**Impact:**
*   Disrupts user experience.
*   Leads to data loss if the user is in the middle of typing.
*   Diminishes overall reliability and usability of the keyboard.

**Possible Causes (Requires further investigation):**
*   **Memory Leaks:** The application might be consuming excessive memory over time, leading to the Android system terminating it to free up resources.
*   **ANR (Application Not Responding) Errors:** Long-running operations on the main thread could cause the application to become unresponsive, leading to the system terminating it.
*   **Uncaught Exceptions/Crashes:** Critical errors or unhandled exceptions in the code could lead to an application crash.
*   **Resource Exhaustion:** Excessive use of CPU, battery, or other system resources could trigger the system's OOM (Out Of Memory) killer or other stability mechanisms.
*   **Background Process Limitations:** Android aggressively manages background processes, and if the keyboard is not correctly managing its lifecycle or background tasks, it might be terminated.
*   **Concurrency Issues:** Race conditions or deadlocks in multi-threaded code could lead to instability.

**Recommendation:**
*   Implement comprehensive logging to capture crash reports and ANR events.
*   Utilize Android profiling tools (e.g., Android Studio Profiler) to monitor CPU, memory, and battery usage during typical and extended use.
*   Review code for potential memory leaks, especially in `OwnboardIME.kt` and related lifecycle methods.
*   Ensure all long-running operations are off the main UI thread.
*   Implement robust error handling and exception catching.
*   Thoroughly test on various Android devices and versions to identify device-specific or OS-specific issues.

---

## Codebase Investigation Findings

### Summary of Critical Issues

The investigation revealed critical architectural issues stemming primarily from heavy reliance on a static singleton pattern (`OwnboardIME.ime`) and global static listeners (`Key.shift`, etc.). This design choice leads to severe tight coupling, making the code difficult to maintain, test, and prone to bugs. Significant performance and stability risks are present due to blocking operations on the main thread, including database queries (e.g., `LayoutDatabase.getLayoutByLang`) and the complete, from-scratch inflation of the keyboard UI (`buildKeyboard`) on every language switch. Additionally, UI rendering in the base `Key` class is inefficient, and the database migration strategy in `LayoutDatabase` involves data loss.

### Immediate Priorities for Improvement:

1.  **Dependency Management:** Eliminate static singletons and adopt proper dependency injection techniques.
2.  **Concurrency:** Move all database and file I/O operations off the main thread using Kotlin coroutines or other asynchronous mechanisms.
3.  **UI Optimization:** Refactor keyboard view creation to reuse and update existing views instead of destroying and recreating them entirely.
4.  **Data Persistence:** Implement a non-destructive database migration strategy for `LayoutDatabase`.

### Detailed Findings by File/Component:

#### `app/src/main/java/com/ownboard/app/OwnboardIME.kt`

*   **Role:** Core input service, central point of many architectural problems.
*   **Issues:**
    *   **Static Singleton (`ime` companion object):** Creates a global singleton, tightly coupling other components to it. This makes testing difficult and leads to an inflexible design.
    *   **Main Thread Blocking:** The `buildKeyboard` method performs several synchronous, potentially long-running operations directly on the main thread, including:
        *   JSON parsing (`loadKeyboardFromDB`).
        *   Database access (`loadKeyboardFromDB`).
        *   Full view hierarchy inflation.
    *   **Inefficient UI Creation:** The keyboard UI is completely destroyed and rebuilt (`buildKeyboard`) every time the keyboard is shown or the language changes. This is a major source of performance bottlenecks, lag, and ANR (Application Not Responding) risks.
*   **Key Symbols:** `ime (companion object)`, `buildKeyboard`, `loadKeyboardFromDB`, `onStartInputView`

#### `app/src/main/java/com/ownboard/app/Key.kt`

*   **Role:** Base class for all keyboard keys.
*   **Issues:**
    *   **Inefficient `onDraw`:** The `onDraw` method instantiates a new `Paint` object on every call. This creates unnecessary object allocations, leading to increased garbage collection activity and potential UI stuttering. `Paint` objects should be initialized once and reused.
    *   **Global Mutable State (Companion Object):** The `companion object` holds globally mutable state for modifier keys (`capslock`, `ctrl`, `alt`, `shift`). This is an anti-pattern that can lead to:
        *   Race conditions.
        *   Unpredictable behavior due to concurrent modifications from different parts of the application.
        *   Difficulty in tracking state changes and debugging.
    *   **Tight Coupling:** Contains hard dependencies on the `OwnboardIME.ime` singleton for sending key events, further contributing to the rigid architecture.
*   **Key Symbols:** `onDraw`, `onTouchEvent`, `capslock, ctrl, alt, shift (companion object)`

#### `app/src/main/java/com/ownboard/app/All.kt`

*   **Role:** Subclass of `Key`, demonstrating consequences of the overall architecture.
*   **Issues:**
    *   **Reliance on Global State:** Depends on the static `OwnboardIME.ime` for its functionality and directly accesses/manipulates global state defined in `Key.kt`, exacerbating tight coupling and state management problems.
    *   **Brittle Configuration:** Behavior is configured via string-matching in setters, which is prone to errors, lacks type safety, and is hard to refactor.
    *   **Inefficient Popups:** Logic for showing popups on long press involves creating new views repeatedly, which can impact performance.
*   **Key Symbols:** `onClickFn`, `onLongPressFn`, `params`

#### `app/src/main/java/com/ownboard/app/db/LayoutDatabase.kt`

*   **Role:** Manages database access for keyboard layouts.
*   **Issues:**
    *   **Synchronous I/O on Main Thread:** All public methods (`getLayoutByLang`, `updateLayout`, etc.) perform synchronous I/O operations directly on the calling thread. Since these are invoked from `OwnboardIME` (on the main thread), this is a major source of ANRs and overall application unresponsiveness.
    *   **Destructive Migration (`onUpgrade`):** The `onUpgrade` method, which handles database schema changes, indiscriminately deletes all user-customized layout data (`db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)`) when the schema version increments. This results in data loss for users on application updates.
*   **Key Symbols:** `getLayoutByLang`, `onUpgrade`, `loadJSONFromAsset`