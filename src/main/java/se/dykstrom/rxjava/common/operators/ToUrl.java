package se.dykstrom.rxjava.common.operators;

import rx.Observable.Operator;
import rx.Subscriber;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An {@link Operator} that implements a mapping from String to URL.
 */
public class ToUrl implements Operator<URL, String> {
    @Override
    public Subscriber<? super String> call(Subscriber<? super URL> subscriber) {
        return new Subscriber<String>(subscriber) {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(String spec) {
                try {
                    subscriber.onNext(new URL(spec));
                } catch (MalformedURLException e) {
                    subscriber.onError(e);
                }
            }
        };
    }
}
