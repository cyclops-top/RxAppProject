package justin.rxapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;


/**
 * @author justin on 2017/04/19 16:07
 * @version V1.0
 */
public class RxAppHelper implements IRxAppSupport {

    private final AtomicInteger sAtomicInteger = new AtomicInteger(0x888);
    private SparseArray<BehaviorSubject<Permission[]>> mPermissionObservable = new SparseArray<>();
    private SparseArray<BehaviorSubject<ActivityResult>> mActivityResultObservable = new SparseArray<>();
    private HashMap<String, ObservableBroadcastReceiver> mBroadcastReceivers = new HashMap<>();

    private final Fragment mFragment;
    private final android.support.v4.app.Fragment mFragmentV4;


    private BehaviorSubject<LifeState> mLife = BehaviorSubject.createDefault(LifeState.NOT_INIT);

    public RxAppHelper(Fragment fragment) {
        this(fragment, null);
    }

    public RxAppHelper(android.support.v4.app.Fragment fragment) {
        this(null, fragment);
    }

    public RxAppHelper(Fragment fragment, android.support.v4.app.Fragment fragmentV4) {
        mFragment = fragment;
        mFragmentV4 = fragmentV4;
        if (mFragment == null && mFragmentV4 == null) {
            throw new IllegalArgumentException("fragment can not null");
        }
    }

    private Activity getActivity() {
        if (mFragmentV4 != null) {
            return mFragmentV4.getActivity();
        } else {
            return mFragment.getActivity();
        }
    }


    protected void init() {
        mLife.onNext(LifeState.INIT);
    }

    protected void release() {
        mLife.onNext(LifeState.RELEASE);
    }

    @Override
    public <T> ObservableTransformer<T, T> bindLife() {
        return new CheckLifeCycleTransformer<>(mLife);
    }

    /**
     * 通过传入的Fragment 请求权限
     *
     * @param permissions 权限
     * @param requestCode 请求权限id
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(@NonNull String[] permissions, int requestCode) {
        if (mFragmentV4 != null) {
            mFragmentV4.requestPermissions(permissions, requestCode);
        } else {
            mFragment.requestPermissions(permissions, requestCode);
        }
    }

    /**
     * 通过传入的Fragment startActivityForResult
     *
     * @param intent      需要启动的activity信息
     * @param requestCode 请求code
     * @param options     动画参数
     */
    private void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (mFragmentV4 != null) {
            mFragmentV4.startActivityForResult(intent, requestCode, options);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mFragment.startActivityForResult(intent, requestCode, options);
            } else {
                mFragment.startActivityForResult(intent, requestCode);
            }
        }
    }

    public boolean selfPermissionGranted(String permission) {
        return PermissionChecker.checkSelfPermission(getActivity(), permission)
                == PermissionChecker.PERMISSION_GRANTED;
    }

    /**
     * 获取目前的进程已确定的权限
     *
     * @param permissions 权限
     * @return 权限状态数组
     */
    Permission[] getCurrPermission(String... permissions) {
        Permission[] permissionResult = new Permission[permissions.length];
        int index = 0;
        for (String permission : permissions) {
            boolean isAllow = selfPermissionGranted(permission);
            permissionResult[index] = new Permission(permission, isAllow);
            index++;
        }
        return permissionResult;
    }

    /**
     * 判定和请求权限，并返回是否全部权限都认可的Observable
     * <p>在Android M 以下版本会直接返回True，如需兼容M以下请自行try catch</p>
     * @param permissions 权限
     * @return 结果Observable
     */
    @Override
    public Observable<Boolean> ensure(String... permissions) {
        return requestPermission(permissions)
                .flatMap(new Function<Permission[], ObservableSource<Permission>>() {
                    @Override
                    public ObservableSource<Permission> apply(@NonNull Permission[] permissions) throws Exception {
                        return Observable.fromArray(permissions);
                    }
                })
                .filter(new Predicate<Permission>() {
                    @Override
                    public boolean test(@NonNull Permission permission) throws Exception {
                        return !permission.isGranted();
                    }
                })
                .toList()
                .flatMapObservable(new Function<List<Permission>, ObservableSource<? extends Boolean>>() {
                    @Override
                    public ObservableSource<? extends Boolean> apply(@NonNull List<Permission> permissions) throws Exception {
                        return Observable.just(permissions.size() == 0);
                    }
                });
    }

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
    @Override
    public Observable<Permission[]> requestPermission(final String... permissions) {
        return Observable.just(permissions)
                .flatMap(new Function<String[], ObservableSource<? extends Permission[]>>() {
                    @Override
                    public ObservableSource<? extends Permission[]> apply(@NonNull final String[] per) throws Exception {
                        Permission[] currPermissions = RxAppHelper.this.getCurrPermission(permissions);
                        Observable<Permission[]> observable;

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            observable = Observable.just(currPermissions);
                        } else {
                            int requestCode = sAtomicInteger.getAndIncrement();
                            List<String> notAllowList = new ArrayList<>();
                            final List<Permission> allowList = new ArrayList<>();
                            for (Permission permission : currPermissions) {
                                if (!permission.isGranted()) {
                                    notAllowList.add(permission.getPermission());
                                } else {
                                    allowList.add(permission);
                                }
                            }
                            if (notAllowList.size() == 0) {
                                observable = Observable.just(currPermissions);
                            } else {
                                observable = BehaviorSubject.create();
                                mPermissionObservable.put(requestCode, (BehaviorSubject<Permission[]>) observable);
                                String[] requestPermissions = new String[notAllowList.size()];
                                int index = 0;
                                for(String permission: notAllowList){
                                    requestPermissions[index] = permission;
                                    index++;
                                }
                                requestPermissions(requestPermissions, requestCode);
                                observable.map(new Function<Permission[], Permission[]>() {
                                    @Override
                                    public Permission[] apply(@NonNull Permission[] permissions) throws Exception {
                                        Permission[] result = new Permission[allowList.size() + permissions.length];
                                        int index = 0;
                                        for (Permission permission : allowList) {
                                            result[index] = permission;
                                            index++;
                                        }
                                        for (Permission permission : permissions) {
                                            result[index] = permission;
                                            index++;
                                        }
                                        return result;
                                    }
                                });
                            }
                        }
                        return observable;
                    }
                });
    }


    @Override
    public Observable<Permission> requestPermission(String permissions) {
        return requestPermission(new String[]{permissions}).map(new Function<Permission[], Permission>() {
            @Override
            public Permission apply(@NonNull Permission[] results) throws Exception {
                return results[0];
            }
        });
    }

    /**
     * 由fragment 回调的请求权限结果
     *
     * @param requestCode  请求code
     * @param permissions  权限
     * @param grantResults 状态
     */
    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        BehaviorSubject<Permission[]> observable = mPermissionObservable.get(requestCode, null);
        if (observable != null) {
            int len = permissions.length;
            Permission[] permissionResult = new Permission[len];
            for (int i = 0; i < len; i++) {
                permissionResult[i] = new Permission(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
            observable.onNext(permissionResult);
            observable.onComplete();
            mPermissionObservable.remove(requestCode);
        }
    }

    /**
     * startActivityForResult 并返回activity结果的Observable
     *
     * @param intent 需要启动的activity信息
     * @param opt    参数，一般为动画参数
     * @return activity结果的Observable
     */
    @Override
    public Observable<ActivityResult> startActivityForObservable(Intent intent, Bundle opt) {
        int requestCode = sAtomicInteger.getAndIncrement();
        BehaviorSubject<ActivityResult> observable = BehaviorSubject.create();
        mActivityResultObservable.put(requestCode, observable);
        startActivityForResult(intent, requestCode, opt);
        return observable;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BehaviorSubject<ActivityResult> observable = mActivityResultObservable.get(requestCode);
        if (observable != null) {
            observable.onNext(new ActivityResult(resultCode, data));
            observable.onComplete();
            mActivityResultObservable.remove(requestCode);
        }
    }


    @Override
    public Observable<Intent> broadcast(String... filters) {
        return broadcast(false, filters);
    }

    /**
     * 广播接收
     *
     * @param hasLastData 是否传最后一次广播参数
     * @param filters     actions
     * @return data's Observable
     */
    @Override
    public Observable<Intent> broadcast(final boolean hasLastData, final String... filters) {
        final ObservableEmitter[] mObservableEmitter = new ObservableEmitter[1];
        return Observable.create(new ObservableOnSubscribe<Intent>() {
                                     @Override
                                     public void subscribe(@NonNull ObservableEmitter<Intent> e) throws Exception {
                                         mObservableEmitter[0] = e;
                                         for (String filter : filters) {
                                             ObservableBroadcastReceiver broadcast = mBroadcastReceivers.get(filter);
                                             if (broadcast == null) {
                                                 broadcast = new ObservableBroadcastReceiver();
                                                 IntentFilter intentFilter = new IntentFilter();
                                                 intentFilter.addAction(filter);
                                                 Intent last = RxAppHelper.this.getActivity().registerReceiver(broadcast, intentFilter);
                                                 broadcast.setLastData(last);
                                                 mBroadcastReceivers.put(filter, broadcast);
                                             }
                                             broadcast.add(hasLastData, e);
                                         }
                                     }
                                 }
        ).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                ObservableEmitter e = mObservableEmitter[0];
                if (e == null) {
                    return;
                }
                for (String filter : filters) {
                    ObservableBroadcastReceiver broadcast = mBroadcastReceivers.get(filter);
                    if (broadcast != null) {
                        //noinspection unchecked
                        broadcast.remove((ObservableEmitter<Intent>) e);
                        if (broadcast.size() == 0) {
                            RxAppHelper.this.getActivity().unregisterReceiver(broadcast);
                            mBroadcastReceivers.remove(filter);
                        }
                    }
                }
            }
        });
    }

    public void unregisterAllReceiver() {
        for (Map.Entry<String, ObservableBroadcastReceiver> entry : mBroadcastReceivers.entrySet()) {
            ObservableBroadcastReceiver val = entry.getValue();
            getActivity().unregisterReceiver(val);
        }
    }

    private class ObservableBroadcastReceiver extends BroadcastReceiver {
        private List<ObservableEmitter<Intent>> emitters = new ArrayList<>();
        private Intent lastData;

        public void add(boolean hasLastData, ObservableEmitter<Intent> emitter) {
            emitters.add(emitter);
            if (hasLastData && lastData != null) {
                emitter.onNext(lastData);
            }
        }

        void setLastData(Intent data) {
            lastData = data;
        }

        public void remove(ObservableEmitter<Intent> emitter) {
            emitters.remove(emitter);
        }

        public int size() {
            return emitters.size();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            lastData = intent;
            for (ObservableEmitter<Intent> emitter : emitters) {
                emitter.onNext(intent);
            }
        }
    }

}
