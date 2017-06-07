@file:Suppress("unused")

package justin.rxapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.PermissionChecker
import android.util.SparseArray
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author justin on 2017/04/19 16:07
 * *
 * @version V1.0
 */
class RxAppHelper @JvmOverloads constructor(private val mFragment: Fragment?, private val mFragmentV4: android.support.v4.app.Fragment? = null) : IRxAppSupport {

    private val sAtomicInteger = AtomicInteger(0x888)
    private val mPermissionObservable = SparseArray<BehaviorSubject<Array<Permission>>>()
    private val mActivityResultObservable = SparseArray<BehaviorSubject<ActivityResult>>()
    private val mBroadcastReceivers = HashMap<String, ObservableBroadcastReceiver>()
    private val mBroadcastObservables = HashMap<String, Observable<Intent>>()


    private val mLife = BehaviorSubject.createDefault(LifeState.NOT_INIT)

    constructor(fragment: android.support.v4.app.Fragment) : this(null, fragment)

    init {
        if (mFragment == null && mFragmentV4 == null) {
            throw IllegalArgumentException("fragment can not null")
        }
    }

    private val activity: Activity
        get() {
            if (mFragmentV4 != null) {
                return mFragmentV4.activity!!
            } else {
                return mFragment!!.activity!!
            }
        }


    fun init() {
        mLife.onNext(LifeState.INIT)
    }

    fun release() {
        mLife.onNext(LifeState.RELEASE)
    }

    public override fun <T> bindLife(): ObservableTransformer<T, T> {
        return CheckLifeCycleTransformer(mLife)
    }

    /**
     * 通过传入的Fragment 请求权限

     * @param permissions 权限
     * @param requestCode 请求权限id
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (mFragmentV4 != null) {
            mFragmentV4.requestPermissions(permissions, requestCode)
        } else {
            mFragment!!.requestPermissions(permissions, requestCode)
        }
    }

    /**
     * 通过传入的Fragment startActivityForResult

     * @param intent      需要启动的activity信息
     * @param requestCode 请求code
     * @param options     动画参数
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        if (mFragmentV4 != null) {
            mFragmentV4.startActivityForResult(intent, requestCode, options)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mFragment!!.startActivityForResult(intent, requestCode, options)
            } else {
                mFragment!!.startActivityForResult(intent, requestCode)
            }
        }
    }

    fun selfPermissionGranted(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(activity, permission) == PermissionChecker.PERMISSION_GRANTED
    }


    internal fun getCurrPermission(vararg permissions: String): Array<Permission> {
        val permissionResult = arrayOfNulls<Permission>(permissions.size)
        for ((index, permission) in permissions.withIndex()) {
            val isAllow = selfPermissionGranted(permission)
            permissionResult[index] = Permission(permission, isAllow)
        }
        return permissionResult.requireNoNulls()
    }


    public override fun ensure(vararg permissions: String): Observable<Boolean> {
        return requestPermission(*permissions)
                .flatMap { Observable.fromArray(*it) }
                .filter { !it.isGranted }
                .toList()
                .flatMapObservable{ Observable.just(it.size == 0) }
    }


    public override fun requestPermission(vararg permissions: String): Observable<Array<Permission>> {
        return Observable.just(permissions)
                .flatMap<Array<Permission>> {
                    val currPermissions = getCurrPermission(*permissions)
                    val observable: Observable<Array<Permission>>

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        observable = Observable.just(currPermissions)
                    } else {
                        val requestCode = sAtomicInteger.getAndIncrement()
                        val notAllowList = ArrayList<String>()
                        val allowList = ArrayList<Permission>()
                        for (permission in currPermissions) {
                            if (!permission.isGranted) {
                                notAllowList.add(permission.permission)
                            } else {
                                allowList.add(permission)
                            }
                        }
                        if (notAllowList.size == 0) {
                            observable = Observable.just(currPermissions)
                        } else {
                            observable = BehaviorSubject.create<Array<Permission>>()
                            mPermissionObservable.put(requestCode, observable as BehaviorSubject<Array<Permission>>)
                            val requestPermissions = arrayOfNulls<String>(notAllowList.size)
                            for ((index, permission) in notAllowList.withIndex()) {
                                requestPermissions[index] = permission
                            }
                            requestPermissions(requestPermissions.requireNoNulls(), requestCode)
                            observable.map<Array<Permission>> { permissions ->
                                val result = arrayOfNulls<Permission>(allowList.size + permissions.size)
                                var index = 0
                                for (permission in allowList) {
                                    result[index] = permission
                                    index++
                                }
                                for (permission in permissions) {
                                    result[index] = permission
                                    index++
                                }
                                result.requireNoNulls()
                            }
                        }
                    }
                    observable
                }
    }


    public override fun requestPermission(permission: String): Observable<Permission> {
        return requestPermission(*arrayOf(permission)).map { it[0] }
    }

    /**
     * 由fragment 回调的请求权限结果

     * @param requestCode  请求code
     * *
     * @param permissions  权限
     * *
     * @param grantResults 状态
     */
    public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val observable = mPermissionObservable.get(requestCode, null)
        if (observable != null) {
            val len = permissions.size
            val permissionResult = arrayOfNulls<Permission>(len)
            for (i in 0..len - 1) {
                permissionResult[i] = Permission(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED)
            }
            observable.onNext(permissionResult.requireNoNulls())
            observable.onComplete()
            mPermissionObservable.remove(requestCode)
        }
    }

    public override fun startActivityForObservable(intent: Intent, opt: Bundle?): Observable<ActivityResult> {
        val requestCode = sAtomicInteger.getAndIncrement()
        val observable = BehaviorSubject.create<ActivityResult>()
        mActivityResultObservable.put(requestCode, observable)
        startActivityForResult(intent, requestCode, opt)
        return observable
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val observable = mActivityResultObservable.get(requestCode)
        if (observable != null) {
            observable.onNext(ActivityResult(resultCode, data))
            observable.onComplete()
            mActivityResultObservable.remove(requestCode)
        }
    }


    public override fun broadcast(vararg filters: String): Observable<Intent> {
        return broadcast(true, *filters)
    }

    private fun getBroadcastObservable(hasLastData: Boolean, filter: String): Observable<Intent> {
        val out = mBroadcastObservables[filter]
        if (out != null) {
            return out
        }
        val subject = BehaviorSubject.create<Intent>()
        val receiver = ObservableBroadcastReceiver(subject)
        val intentFilter = IntentFilter()
        intentFilter.addAction(filter)
        val last = this@RxAppHelper.activity.registerReceiver(receiver, intentFilter)
        mBroadcastReceivers.put(filter, receiver)
        if (last != null && hasLastData) {
            subject.onNext(last)
        }
        val size = intArrayOf(0)
        val observable = subject.doOnLifecycle({ size[0]++ }, {
            size[0]--
            if (size[0] <= 0) {
                this@RxAppHelper.activity.unregisterReceiver(receiver)
                mBroadcastObservables.remove(filter)
                mBroadcastReceivers.remove(filter)
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
        mBroadcastObservables.put(filter, observable)
        return observable
    }


    public override fun broadcast(useStickData: Boolean, vararg filters: String): Observable<Intent> {
        if (filters.isEmpty()) {
            throw IllegalArgumentException("filters can't empty")
        }
        var observable: Observable<Intent>? = null
        for (filter in filters) {
            val next = getBroadcastObservable(useStickData, filter)
            if (observable == null) {
                observable = next
            } else {
                observable = observable.mergeWith(next)
            }
        }
        return observable!!

    }

    fun unregisterAllReceiver() {
        for (entry in mBroadcastReceivers.entries) {
            val receiver = entry.value
            activity.unregisterReceiver(receiver)
            receiver.onComplete()
        }
    }

    internal inner class ObservableBroadcastReceiver(private val mSubject: BehaviorSubject<Intent>) : BroadcastReceiver() {

        public override fun onReceive(context: Context, intent: Intent) {
            mSubject.onNext(intent)
        }

        fun onComplete() {
            mSubject.onComplete()
        }

    }

}
