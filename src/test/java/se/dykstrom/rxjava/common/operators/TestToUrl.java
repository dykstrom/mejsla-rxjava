package se.dykstrom.rxjava.common.operators;

import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestToUrl {

    private static final String URL_SPEC = "http://www.google.com";

    private final ToUrl toUrl = new ToUrl();

    private final TestSubscriber<URL> subscriber = new TestSubscriber<>();

    private URL expectedUrl;

    @Before
    public void setUp() throws Exception {
        expectedUrl = new URL(URL_SPEC);
    }

    @Test
    public void testToUrl() throws Exception {
        printRun("testToUrl", () -> {
            Subscriber<? super String> source = toUrl.call(subscriber);

            source.onNext(URL_SPEC);
            source.onCompleted();
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertValues(expectedUrl);
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testToUrl_MalformedURLException() throws Exception {
        printRun("testToUrl_MalformedURLException", () -> {
            Subscriber<? super String> source = toUrl.call(subscriber);

            source.onNext("foo");
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertError(MalformedURLException.class);
        });
    }

    @Test
    public void testToUrl_WithLift() throws Exception {
        printRun("testToUrl_WithLift", () -> {
            Observable<String> source = Observable.just(URL_SPEC);
            Observable<URL> observable = source.lift(new ToUrl());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertValues(expectedUrl);
            subscriber.assertNoErrors();
            subscriber.assertCompleted();
        });
    }

    @Test
    public void testToUrl_WithLift_MalformedURLException() throws Exception {
        printRun("testToUrl_WithLift_MalformedURLException", () -> {
            Observable<String> source = Observable.just("foo");
            Observable<URL> observable = source.lift(new ToUrl());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertError(MalformedURLException.class);
        });
    }
}
