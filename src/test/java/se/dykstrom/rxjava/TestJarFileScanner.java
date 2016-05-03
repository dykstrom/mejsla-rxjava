package se.dykstrom.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static se.dykstrom.rxjava.common.utils.Utils.createZipFile;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestJarFileScanner {

    private final TestSubscriber<String> subscriber = new TestSubscriber<>();
    private final TestSubscriber<Integer> integerSubscriber = new TestSubscriber<>();

    @Test
    public void testClassCountObs() throws Exception {
        List<String> fileNames = Arrays.asList("foo.class", "bar.class", "axe.class");
        List<Integer> expectedCount = Collections.singletonList(fileNames.size());
        File file = createZipFile(fileNames);

        printRun("testClassCountObs", () -> {
            Observable<Integer> observable = JarFileScanner.classCountObs(Observable.just(file.toPath()));

            observable.subscribe(integerSubscriber);
            integerSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            integerSubscriber.assertReceivedOnNext(expectedCount);
            integerSubscriber.assertNoErrors();
        });
    }

    @Test
    public void testClassFileObs() throws Exception {
        List<String> expectedFileNames = Arrays.asList("foo.class", "bar.class", "axe.class");
        File file = createZipFile(expectedFileNames);

        printRun("testClassFileObs", () -> {
            Observable<String> observable = JarFileScanner.classFileObs(file.toPath());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertReceivedOnNext(expectedFileNames);
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testClassFileObs_Empty() throws Exception {
        File file = createZipFile(Collections.emptyList());

        printRun("testClassFileObs_Empty", () -> {
            Observable<String> observable = JarFileScanner.classFileObs(file.toPath());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testClassFileObs_IOException() throws Exception {
        Path path = Paths.get("c:/Temp/does-not-exist.zip");
        String encoding = StandardCharsets.UTF_8.name();

        printRun("testClassFileObs_IOException", () -> {
            Observable<String> observable = JarFileScanner.classFileObs(path);

            PrintStream stderr = System.err;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                // Create a PrintStream to use instead of stderr
                PrintStream errorStream = new PrintStream(outputStream, true, encoding);
                System.setErr(errorStream);

                observable.subscribe(subscriber);
                subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

                assertTrue(outputStream.toString(encoding).contains("FileNotFoundException"));
                subscriber.assertNoValues();
                subscriber.assertNoErrors();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                System.setErr(stderr);
            }
        });
    }
}
