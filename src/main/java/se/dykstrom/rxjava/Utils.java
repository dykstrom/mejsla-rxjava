package se.dykstrom.rxjava;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {

    /**
     * Returns the greatest common divisor (GCD) of a and b.
     */
    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }

    /**
     * Runs the given runnable, and returns the time it took for the runnable to complete,
     * measured in milliseconds.
     */
    public static long timeRun(Runnable runnable) {
        long start = System.currentTimeMillis();
        long stop;
        try {
            runnable.run();
        } finally {
            stop = System.currentTimeMillis();
        }
        return stop - start;
    }

    /**
     * Returns a string whose value is the given string, with any leading and trailing
     * non-alphabetic characters removed.
     */
    public static String trimNonAlpha(String string) {
        int start = 0;
        int end = string.length();

        while (!Character.isAlphabetic(string.charAt(start))) start++;
        while (!Character.isAlphabetic(string.charAt(end - 1))) end--;

        return string.substring(start, end);
    }

    /**
     * Returns an {@link Observable} that emits the contents of the given {@link File} line by line.
     */
    public static Observable<String> fromFile(File file) {
        return Observable.create(subscriber -> {
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

    public static <T> Subscription subscribePrint(Observable<T> observable, String name) {
        return observable.subscribe(
                (v) -> System.out.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + ": " + v), (e) -> {
                    System.err.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + " error:");
                    System.err.println(Arrays
                                    .stream(e.getStackTrace())
                                    .limit(5L)
                                    .map(stackEl -> "  " + stackEl)
                                    .collect(Collectors.joining("\n"))
                    );
                }, () -> System.out.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + ": ended!"));
    }
}
