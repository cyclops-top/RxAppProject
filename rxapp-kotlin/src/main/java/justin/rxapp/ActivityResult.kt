package justin.rxapp

import android.app.Activity
import android.content.Intent

/**
 * @author justin on 2017/04/19 13:29
 * *
 * @version V1.0
 */
data class ActivityResult(val code: Int, val data: Intent) {

    val isOk: Boolean
        get() = code == Activity.RESULT_OK

    val isCancel: Boolean
        get() = code == Activity.RESULT_CANCELED

    val isFirstUser: Boolean
        get() = code == Activity.RESULT_FIRST_USER
}
