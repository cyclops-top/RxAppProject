package justin.rxapp;

import android.content.Intent;
import android.os.Bundle;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

/**
 * @author justin on 2017/04/19 15:51
 * @version V1.0
 */
public interface IRxAppSupport {
    /**
     * 确保每个权限都允许
     *
     * @param permissions 权限
     * @return 是否都有权限
     */
    Observable<Boolean> ensure(String... permissions);

    /**
     * 判定和请求权限，并返回其结果的Observable
     * <p>注意以下几点：</p>
     * <p>  1.在Android M 以下的版本会直接返回所有权限已获取</p>
     * <p>  2.请求顺序和返回顺序是不同的</p>
     * <p>  3.因为针对某些机型的适配问题，已有的权限不再获取，直接返回。
     * 如在小米5上对已有权限再次进行请求，如果用户拒绝就会崩溃</p>
     *
     * @param permissions 需请求的权限
     * @return 请求权限的状态Observable
     */
    Observable<Permission[]> requestPermission(String... permissions);

    /**
     * 请求某个权限
     *
     * @param permission 权限
     * @return 权限结果
     */
    Observable<Permission> requestPermission(String permission);


    /**
     * startActivityForResult 并返回activity结果的Observable
     *
     * @param intent 需要启动的activity信息
     * @param opt    参数，一般为动画参数
     * @return activity结果的Observable
     */
    Observable<ActivityResult> startActivityForObservable(Intent intent, Bundle opt);

    /**
     * 注册广播
     *
     * @param filters 广播列表
     * @return 接收到广播的Observable
     */
    Observable<Intent> broadcast(String... filters);

    /**
     * @param useStickData 是否使用Stick广播的最后一次数据
     * @param filters      广播列表
     * @return 接收到广播的Observable
     */
    Observable<Intent> broadcast(boolean useStickData, String... filters);

    /**
     * 对一个Observable绑定生命周期
     * <p>Sample：</p>
     * <pre>
     * <code>
     *
     * Observable.interval(5, TimeUnit.SECONDS)
     *   .compose(bindLife(MainActivity.this))
     *   .observeOn(AndroidSchedulers.mainThread())
     *   .subscribe(...)
     *
     * </code>
     * </pre>
     */
    <T> ObservableTransformer<T, T> bindLife();
}
