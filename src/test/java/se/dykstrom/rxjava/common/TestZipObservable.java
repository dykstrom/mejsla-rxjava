package se.dykstrom.rxjava.common;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static se.dykstrom.rxjava.common.utils.Utils.createZipFile;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestZipObservable {

    private final TestSubscriber<ZipEntry> subscriber = new TestSubscriber<>();

    @Test
    public void testFromPath() throws Exception {
        List<String> expectedFileNames = Arrays.asList("foo.txt", "bar.txt", "axe.txt");
        File file = createZipFile(expectedFileNames);

        printRun("testFromPath", () -> {
            Observable<ZipEntry> observable = ZipObservable.fromPath(file.toPath());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> actualFileNames = subscriber.getOnNextEvents().stream().map(ZipEntry::getName).collect(toList());
            assertEquals(expectedFileNames, actualFileNames);
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testFromPath_Empty() throws Exception {
        File file = createZipFile(Collections.emptyList());

        printRun("testFromPath_Empty", () -> {
            Observable<ZipEntry> observable = ZipObservable.fromPath(file.toPath());

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testFromPath_IOException() {
        Path path = Paths.get("c:/Temp/does-not-exist.zip");

        printRun("testFromPath_IOException", () -> {
            Observable<ZipEntry> observable = ZipObservable.fromPath(path);

            observable.subscribe(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertError(NoSuchFileException.class);
        });
    }
}
