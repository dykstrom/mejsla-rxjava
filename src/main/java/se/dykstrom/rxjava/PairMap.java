package se.dykstrom.rxjava;

import java.util.function.BiFunction;

import rx.Observable.Operator;
import rx.Subscriber;

/**
 * An {@link Operator} that implements a
 * <a href="http://amaembo.github.io/streamex/javadoc/one/util/streamex/StreamEx.html#pairMap-java.util.function.BiFunction-">pairMap</a>.
 *
 * @param <T> The type of the input items, that is, the items emitted by the source Observable.
 * @param <R> The type of the output items, that is, the items emitted by this Operator.
 */
public class PairMap<T, R> implements Operator<R, T> {

    private final BiFunction<T, T, R> function;

    /**
     * Creates a new PairMap Operator that applies the given function to all pairs of items emitted by the source Observable.
     */
    PairMap(BiFunction<T, T, R> function) {
        this.function = function;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super R> subscriber) {
        return new Subscriber<>(subscriber) {

            private T previous;

            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(T current) {
                if (previous != null) {
                    subscriber.onNext(function.apply(previous, current));
                } else {
                    // We swallow 1 item, so add 1 to the total number of requested items,
                    // in case someone downstream has requested a certain numbers of items
                    // Compare with class OperatorFilter
                    request(1);
                }
                previous = current;
            }
        };
    }
}
