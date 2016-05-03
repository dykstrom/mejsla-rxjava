package se.dykstrom.rxjava.common.operators;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
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
                Iterator<String> iterator = stream.iterator();
                while (!subscriber.isUnsubscribed() && iterator.hasNext()) {
                    subscriber.onNext(iterator.next());
                }
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) subscriber.onError(e);
            }
        });
    }
}
