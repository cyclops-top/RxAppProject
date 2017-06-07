@file:Suppress("unused")

package justin.rxapp

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author justin on 2017/06/05 16:17
 * justin@magicare.me
 * @version V1.0
 */
private val FRAGMENT_TAG = "justin.common.rx.RxHelperFragment"



private fun <T> required(finder: T.() -> IRxAppSupport)
        = Lazy { t: T, _ -> t.finder() }


private fun FragmentManager.getRxAppSupport(): IRxAppSupport {
    var fragment = findFragmentByTag(justin.rxapp.FRAGMENT_TAG) as RxHelperFragment?
    if (fragment == null) {
        fragment = RxHelperFragment()
        beginTransaction().add(fragment, justin.rxapp.FRAGMENT_TAG).commitAllowingStateLoss()
        executePendingTransactions()
    }
    return fragment.mRxAppHelper
}

private fun android.support.v4.app.FragmentManager.getRxAppSupport(): IRxAppSupport {
    var fragment = findFragmentByTag(justin.rxapp.FRAGMENT_TAG) as RxHelperFragmentV4?
    if (fragment == null) {
        fragment = RxHelperFragmentV4()
        beginTransaction().add(fragment, justin.rxapp.FRAGMENT_TAG).commitAllowingStateLoss()
        executePendingTransactions()
    }
    return fragment.mRxAppHelper
}

private fun Activity.getRxAppSupport(): IRxAppSupport {
    return fragmentManager.getRxAppSupport()
}

private fun Fragment.getRxAppSupport(): IRxAppSupport {
    return fragmentManager.getRxAppSupport()
}

private fun android.support.v4.app.Fragment.getRxAppSupport(): IRxAppSupport {
    return fragmentManager.getRxAppSupport()
}

private val Activity.rxApp by required {
    getRxAppSupport()
}

private val Fragment.rxApp by required {
    getRxAppSupport()
}

private val android.support.v4.app.Fragment.rxApp by required {
    getRxAppSupport()
}

fun Activity.rxApp(): IRxAppSupport {
    return rxApp
}

fun Fragment.rxApp(): IRxAppSupport {
    return rxApp
}

fun android.support.v4.app.Fragment.rxApp(): IRxAppSupport {
    return rxApp
}

private class Lazy<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }
}