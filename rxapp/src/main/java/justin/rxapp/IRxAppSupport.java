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
    Observable<Boolean> ensure(String... permissions);
    Observable<Permission[]> requestPermission(String... permissions);
    Observable<Permission> requestPermission(String permissions);
    Observable<ActivityResult> startActivityForObservable(Intent intent, Bundle opt);
    Observable<Intent> broadcast(String... filters);
    Observable<Intent> broadcast(boolean hasLastData, String... filters);
    <T> ObservableTransformer<T, T> bindLife();
}
