package se.dykstrom.rxjava.swing.common.operators;

import org.apache.commons.io.IOUtils;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import se.dykstrom.rxjava.swing.common.functions.ThrowingFunc2;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads a document from a URL and returns it as a string.
 */
public class OnSubscribeFromUrl implements OnSubscribe<String> {

    private final URL url;
    private final Scheduler scheduler;
    private final ThrowingFunc2<URL, Charset, String> fromUrlFunction;

    public OnSubscribeFromUrl(URL url, Scheduler scheduler) {
        this(url, scheduler, IOUtils::toString);
    }

    /**
     * Creates a new instance of the operator that reads {@code url} using the specified {@code fromUrlFunction}
     * and operates on the specified {@code scheduler}.
     *
     * @param url The URL to read.
     * @param scheduler The scheduler to operate on.
     * @param fromUrlFunction The function to use for actually reading the URL.
     */
    public OnSubscribeFromUrl(URL url, Scheduler scheduler, ThrowingFunc2<URL, Charset, String> fromUrlFunction) {
        this.url = url;
        this.scheduler = scheduler;
        this.fromUrlFunction = fromUrlFunction;
    }

    @Override
    public void call(Subscriber<? super String> subscriber) {
        scheduler.createWorker().schedule(() -> {
            try {
                String document = fromUrlFunction.call(url, StandardCharsets.UTF_8);
                if (!subscriber.isUnsubscribed()) subscriber.onNext(document);
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) subscriber.onError(e);
            }
        });
    }
}
