package se.dykstrom.rxjava.common;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public final class DirectoryStreamObservable {

    private DirectoryStreamObservable() { }

    /**
     * Returns an Observable that emits all files in the given directory,
     * in the form of {@link Path} objects.
     */
    public static Observable<Path> fromPath(Path path) {
        return fromPath(path, Schedulers.io());
    }

    public static Observable<Path> fromPath(Path path, Scheduler scheduler) {
        return Observable.create(new Observable.OnSubscribe<Path>() {
            @Override
            public void call(Subscriber<? super Path> subscriber) {
                scheduler.createWorker().schedule(() -> {
                    try (Stream<Path> stream = Files.list(path)) {
                        Iterator<Path> iterator = stream.iterator();
                        while (!subscriber.isUnsubscribed() && iterator.hasNext()) {
                            subscriber.onNext(iterator.next());
                        }
                        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                    } catch (IOException e) {
                        if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                    }
                });
            }
        });
    }

    /**
     * Returns an Observable that emits all files in the given directory and its subdirectories,
     * in the form of {@link Path} objects.
     */
    public static Observable<Path> fromPathRecursive(Path path) {
        return fromPathRecursive(path, Schedulers.io());
    }

    public static Observable<Path> fromPathRecursive(Path directory, Scheduler scheduler) {
        return fromPath(directory, scheduler)
                .groupBy(path -> Files.isDirectory(path))
                .flatMap(observable -> observable.getKey() ?
                                observable.flatMap(path -> fromPathRecursive(path, scheduler)) :
                                observable);
    }
}
