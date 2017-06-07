package justin.rxapp

import android.content.Intent
import android.os.Bundle

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

/**
 * @author justin on 2017/04/19 15:51
 * *
 * @version V1.0
 */
interface IRxAppSupport {
    /**
     * 确保每个权限都允许

     * @param permissions 权限
     * *
     * @return 是否都有权限
     */
    fun ensure(vararg permissions: String): Observable<Boolean>

    /**
     * 判定和请求权限，并返回其结果的Observable
     *
     * 注意以下几点：
     *
     *   1.在Android M 以下的版本会直接返回所有权限已获取
     *
     *   2.请求顺序和返回顺序是不同的
     *
     *   3.因为针对某些机型的适配问题，已有的权限不再获取，直接返回。
     * 如在小米5上对已有权限再次进行请求，如果用户拒绝就会崩溃

     * @param permissions 需请求的权限
     * *
     * @return 请求权限的状态Observable
     */
    fun requestPermission(vararg permissions: String): Observable<Array<Permission>>

    /**
     * 请求某个权限

     * @param permission 权限
     * *
     * @return 权限结果
     */
    fun requestPermission(permission: String): Observable<Permission>


    /**
     * startActivityForResult 并返回activity结果的Observable

     * @param intent 需要启动的activity信息
     * *
     * @param opt    参数，一般为动画参数
     * *
     * @return activity结果的Observable
     */
    fun startActivityForObservable(intent: Intent, opt: Bundle?): Observable<ActivityResult>

    /**
     * 注册广播

     * @param filters 广播列表
     * *
     * @return 接收到广播的Observable
     */
    fun broadcast(vararg filters: String): Observable<Intent>

    /**
     * @param useStickData 是否使用Stick广播的最后一次数据
     * *
     * @param filters      广播列表
     * *
     * @return 接收到广播的Observable
     */
    fun broadcast(useStickData: Boolean, vararg filters: String): Observable<Intent>

    /**
     * 对一个Observable绑定生命周期
     *
     * Sample：
     * <pre>
     * `

     * Observable.interval(5, TimeUnit.SECONDS)
     * .compose(bindLife(MainActivity.this))
     * .observeOn(AndroidSchedulers.mainThread())
     * .subscribe(...)

    ` *
    </pre> *
     */
    fun <T> bindLife(): ObservableTransformer<T, T>
}
