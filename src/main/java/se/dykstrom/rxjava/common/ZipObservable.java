package se.dykstrom.rxjava.common;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipObservable {

    private ZipObservable() { }

    /**
     * Returns an Observable that emits all zip entries contained in the file denoted by {@code path}.
     *
     * Scheduler: this version of {@code fromPath} operates by default on the {@code IO} {@link Scheduler}.
     */
    public static Observable<ZipEntry> fromPath(Path path) {
        return fromPath(path, Schedulers.io());
    }

    /**
     * Returns an Observable that emits all zip entries contained in the file denoted by {@code path}.
     *
     * Scheduler: this version of {@code fromPath} operates by default on the {@code IO} {@link Scheduler}.
     */
    public static Observable<ZipEntry> fromPath(Path path, Scheduler scheduler) {
        return Observable.create(new Observable.OnSubscribe<ZipEntry>() {
            @Override
            public void call(Subscriber<? super ZipEntry> subscriber) {
                scheduler.createWorker().schedule(() -> {
                    try (ZipFile zipFile = new ZipFile(path.toFile(), StandardCharsets.UTF_8)) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (!subscriber.isUnsubscribed() && entries.hasMoreElements()) {
                            subscriber.onNext(entries.nextElement());
                        }
                        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                    } catch (IOException e) {
                        if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                    }
                });
            }
        });
    }
}
