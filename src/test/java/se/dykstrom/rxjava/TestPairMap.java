package se.dykstrom.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestPairMap {

    private static final PairMap<Integer, String> COLON_MAPPER = new PairMap<>((a, b) -> a + ":" + b);

    private static final PairMap<Integer, Integer> ADD_MAPPER = new PairMap<>((a, b) -> a + b);

    private final TestSubscriber<String> subscriber = new TestSubscriber<>();

    @Test
    public void testPairMap_Empty() {
        printRun("testPairMap_Empty", () -> {
            Subscriber<? super Integer> source = COLON_MAPPER.call(subscriber);

            source.onCompleted();

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_Single() {
        printRun("testPairMap_Single", () -> {
            Subscriber<? super Integer> source = COLON_MAPPER.call(subscriber);

            source.onNext(1);
            source.onCompleted();

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_Two() {
        printRun("testPairMap_Two", () -> {
            Subscriber<? super Integer> source = COLON_MAPPER.call(subscriber);

            source.onNext(1);
            source.onNext(2);
            source.onCompleted();

            subscriber.assertValues("1:2");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_Three() {
        printRun("testPairMap_Three", () -> {
            Subscriber<? super Integer> source = COLON_MAPPER.call(subscriber);

            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            source.onCompleted();

            subscriber.assertValues("1:2", "2:3");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_Empty() throws Exception {
        printRun("testPairMap_WithLift_Empty", () -> {
            Observable<Integer> source = Observable.empty();
            Observable<String> observable = source.lift(COLON_MAPPER);

            observable.subscribe(subscriber);

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_Single() throws Exception {
        printRun("testPairMap_WithLift_Single", () -> {
            Observable<Integer> source = Observable.just(10);
            Observable<String> observable = source.lift(COLON_MAPPER);

            observable.subscribe(subscriber);

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_Two() throws Exception {
        printRun("testPairMap_WithLift_Two", () -> {
            Observable<Integer> source = Observable.just(10, 20);
            Observable<String> observable = source.lift(COLON_MAPPER);

            observable.subscribe(subscriber);

            subscriber.assertValues("10:20");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_Many() throws Exception {
        printRun("testPairMap_WithLift_Many", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source.lift(COLON_MAPPER);

            observable.subscribe(subscriber);

            subscriber.assertValues("1:2", "2:3", "3:4", "4:5", "5:6", "6:7", "7:8", "8:9", "9:10");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeOne() throws Exception {
        printRun("testPairMap_WithLift_TakeOne", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .lift(COLON_MAPPER)
                    .take(1);

            observable.subscribe(subscriber);

            subscriber.assertValues("1:2");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeTwo() throws Exception {
        printRun("testPairMap_WithLift_TakeTwo", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .lift(COLON_MAPPER)
                    .take(2);

            observable.subscribe(subscriber);

            subscriber.assertValues("1:2", "2:3");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeThree() throws Exception {
        printRun("testPairMap_WithLift_TakeThree", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .lift(COLON_MAPPER)
                    .take(3);

            observable.subscribe(subscriber);

            subscriber.assertValues("1:2", "2:3", "3:4");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeBefore() throws Exception {
        printRun("testPairMap_WithLift_TakeBefore", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .take(3)
                    .lift(COLON_MAPPER);

            observable.subscribe(subscriber);

            // If we take three items from the source (1, 2, and 3),
            // the pairMap will only emit two items (1:2, and 2:3)
            subscriber.assertValues("1:2", "2:3");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeTake() throws Exception {
        printRun("testPairMap_WithLift_TakeTake", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .lift(COLON_MAPPER)
                    .take(3)
                    .map(s -> "-" + s + "-")
                    .take(2);

            observable.subscribe(subscriber);

            // First taking three items from the pairMap Observable,
            // and then taking two out of these three items
            subscriber.assertValues("-1:2-", "-2:3-");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_TakeLast() throws Exception {
        printRun("testPairMap_WithLift_TakeLast", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<String> observable = source
                    .lift(COLON_MAPPER)
                    .takeLast(3);

            observable.subscribe(subscriber);

            // Taking the last three items
            subscriber.assertValues("7:8", "8:9", "9:10");
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testPairMap_WithLift_AddMapper() throws Exception {
        printRun("testPairMap_WithLift_AddMapper", () -> {
            Observable<Integer> source = Observable.range(1, 10);
            Observable<Integer> observable = source.lift(ADD_MAPPER);

            TestSubscriber<Integer> subscriber = new TestSubscriber<>();
            observable.subscribe(subscriber);

            subscriber.assertValues(3, 5, 7, 9, 11, 13, 15, 17, 19);
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }
}
