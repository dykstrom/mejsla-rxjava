package se.dykstrom.rxjava.common.utils;

import rx.Observable;
import rx.Subscription;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                v -> System.out.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + ": " + v),
                e -> {
                    System.err.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + " error: " + e);
                    System.err.println(Arrays
                                    .stream(e.getStackTrace())
                                    .limit(5L)
                                    .map(stackEl -> "  " + stackEl)
                                    .collect(Collectors.joining("\n"))
                    );
                },
                () -> System.out.println(LocalTime.now() + "|" + Thread.currentThread().getName() + "|" + name + ": ended!"));
    }

    /**
     * Creates a temporary zip file that contains one zip entry for each file in {@code fileNames}.
     * The zip file will be deleted on exit.
     */
    public static File createZipFile(List<String> fileNames) throws IOException {
        File file = Files.createTempFile(null, ".zip").toFile();
        file.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            PrintWriter writer = new PrintWriter(zos, true);

            for (String fileName : fileNames) {
                zos.putNextEntry(new ZipEntry(fileName));
                writer.println(fileName);
                zos.closeEntry();
            }
            zos.finish();
        }

        return file;
    }
}
