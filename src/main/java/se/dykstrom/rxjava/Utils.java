package se.dykstrom.rxjava;

import rx.Observable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class Utils {

    /**
     * Runs the given runnable, and returns the time it took for the runnable to complete,
     * measured in microseconds.
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
            try {
                Files.lines(file.toPath()).forEach(line -> {
                    if (!subscriber.isUnsubscribed()) subscriber.onNext(line);
                });
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) subscriber.onError(e);
            }
        });
    }
}
