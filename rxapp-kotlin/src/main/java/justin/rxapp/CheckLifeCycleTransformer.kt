package justin.rxapp

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.annotations.NonNull
import io.reactivex.subjects.BehaviorSubject

/**
 * @author justin on 2017/04/20 10:05
 * *
 * @version V1.0
 */
class CheckLifeCycleTransformer<T>(private val mLife: BehaviorSubject<LifeState>) : ObservableTransformer<T, T> {

    override fun apply(@NonNull upstream: Observable<T>): ObservableSource<T> {
        return upstream.takeUntil(mLife.filter { it == LifeState.RELEASE })
    }
}
