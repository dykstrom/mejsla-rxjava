package se.dykstrom.rxjava.common.utils;

import rx.Observable;
import rx.Subscription;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Collectors;

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
     * Runs the given runnable, and prints to stdout the time it took for the runnable to complete.
     */
    public static void printRun(String name, Runnable runnable) {
        System.out.printf("[%s] finished after %d ms\n", name, timeRun(runnable));
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
