package se.dykstrom.rxjava.swing.mandel;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestLineObservable {

    private static final Parameters PARAMS_WITH_HEIGHT_100 = new Parameters(0, 100, 100, Coordinates.START);

    private TestSubscriber<Line> testSubscriber = new TestSubscriber<>();

    @Test
    public void testSubscribe() {
        printRun("testSubscribe", () -> {
            Observable<Line> observable = LineObservable.fromParameters(PARAMS_WITH_HEIGHT_100);

            observable.subscribe(testSubscriber);

            assertEquals(100, testSubscriber.getOnNextEvents().size());
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();
        });
    }

    @Test
    public void testSubscribeWithMerge() {
        printRun("testSubscribeWithMerge", () -> {
            Observable<Line> observable1 = LineObservable.fromParameters(PARAMS_WITH_HEIGHT_100);
            Observable<Line> observable2 = LineObservable.fromParameters(PARAMS_WITH_HEIGHT_100);
            Observable<Line> observable3 = LineObservable.fromParameters(PARAMS_WITH_HEIGHT_100);

            Observable<Line> merged = Observable.merge(observable1, observable2, observable3);

            merged.subscribe(testSubscriber);

            assertEquals(300, testSubscriber.getOnNextEvents().size());
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();
        });
    }

    @Test
    public void testSubscribeWithFlatMap() {
        printRun("testSubscribeWithFlatMap", () -> {
            Observable<Line> mapped = Observable.just(PARAMS_WITH_HEIGHT_100)
                    .repeat(10)
                    .flatMap(parameters -> LineObservable.fromParameters(parameters)
                            .subscribeOn(Schedulers.computation()));

            mapped.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent();

            assertEquals(1000, testSubscriber.getOnNextEvents().size());
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();
        });
    }
}
