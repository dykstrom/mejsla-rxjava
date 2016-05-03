package se.dykstrom.rxjava.common;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import se.dykstrom.rxjava.common.operators.OnSubscribeFromFile;
import se.dykstrom.rxjava.common.operators.OnSubscribeFromUrl;

import java.io.File;
import java.net.URL;

public final class Observables {

    private Observables() { }

    /**
     * Returns an Observable that reads a document from the given URL, emits it as a single string,
     * and then completes.
     *
     * Scheduler: this version of {@code fromUrl} operates by default on the {@code IO} {@link Scheduler}.
     */
    public static Observable<String> fromUrl(URL url) {
        return fromUrl(url, Schedulers.io());
    }

    /**
     * Returns an Observable that reads a document from the given URL, emits it as a single string,
     * and then completes.
     *
     * Scheduler: you specify which {@link Scheduler} this operator will use.
     */
    public static Observable<String> fromUrl(URL url, Scheduler scheduler) {
        return Observable.create(new OnSubscribeFromUrl(url, scheduler));
    }

    /**
     * Returns an {@link Observable} that emits the contents of the given {@link File} line by line.
     *
     * Scheduler: this version of {@code fromUrl} operates by default on the {@code IO} {@link Scheduler}.
     */
    public static Observable<String> fromFile(File file) {
        return fromFile(file, Schedulers.io());
    }

    /**
     * Returns an {@link Observable} that emits the contents of the given {@link File} line by line.
     *
     * Scheduler: you specify which {@link Scheduler} this operator will use.
     */
    public static Observable<String> fromFile(File file, Scheduler scheduler) {
        return Observable.create(new OnSubscribeFromFile(file, scheduler));
    }
}
