package se.dykstrom.rxjava.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

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
        return Observable.create(subscriber -> scheduler.createWorker().schedule(() -> {
            try (Stream<Path> stream = Files.list(path)) {
                Iterator<Path> iterator = stream.iterator();
                while (!subscriber.isUnsubscribed() && iterator.hasNext()) {
                    subscriber.onNext(iterator.next());
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
        }));
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
                .groupBy(Files::isDirectory)
                .flatMap(observable -> observable.getKey() ?
                                observable.flatMap(path -> fromPathRecursive(path, scheduler)) :
                                observable);
    }
}
