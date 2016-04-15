package se.dykstrom.rxjava.common.operators;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public class OnSubscribeFromFile implements Observable.OnSubscribe<String> {

    private final File file;
    private final Scheduler scheduler;

    public OnSubscribeFromFile(File file, Scheduler scheduler) {
        this.file = file;
        this.scheduler = scheduler;
    }

    @Override
    public void call(Subscriber<? super String> subscriber) {
        scheduler.createWorker().schedule(() -> {
            try (Stream<String> stream = Files.lines(file.toPath())) {
                // Make sure the stream is closed when the subscriber unsubscribes
                subscriber.add(Subscriptions.create(stream::close));
                stream.forEach(line -> {
                    if (!subscriber.isUnsubscribed()) subscriber.onNext(line);
                });
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) subscriber.onError(e);
            }
        });
    }
}
