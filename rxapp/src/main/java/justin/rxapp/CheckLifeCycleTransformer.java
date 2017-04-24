package justin.rxapp;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 * @author justin on 2017/04/20 10:05
 * @version V1.0
 */
public class CheckLifeCycleTransformer<T> implements ObservableTransformer<T, T> {
    private final BehaviorSubject<LifeState> mLife;

    public CheckLifeCycleTransformer(BehaviorSubject<LifeState> life) {
        mLife = life;
    }

    @Override
    public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return upstream.takeUntil(mLife.filter(new Predicate<LifeState>() {
            @Override
            public boolean test(@NonNull LifeState state) throws Exception {
                return state == LifeState.RELEASE;
            }
        }));
    }
}
