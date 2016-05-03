package se.dykstrom.rxjava.common.operators;

import org.junit.Test;
import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestOnSubscribeFromFile {

    private static final Scheduler COMPUTATION_SCHEDULER = Schedulers.computation();

    private final TestSubscriber<String> subscriber = new TestSubscriber<>();

    @Test
    public void testOnSubscribeFromFile() throws Exception {
        List<String> lines = Arrays.asList("abc", "def", "", "ghi", "");
        File file = createFile(lines);

        printRun("testOnSubscribeFromFile", () -> {
            OnSubscribeFromFile onSubscribe = new OnSubscribeFromFile(file, COMPUTATION_SCHEDULER);

            onSubscribe.call(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertReceivedOnNext(lines);
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testOnSubscribeFromFile_Empty() throws Exception {
        File file = createFile(Collections.emptyList());

        printRun("testOnSubscribeFromFile_Empty", () -> {
            OnSubscribeFromFile onSubscribe = new OnSubscribeFromFile(file, COMPUTATION_SCHEDULER);

            onSubscribe.call(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertNoErrors();
        });
    }

    @Test
    public void testOnSubscribeFromFile_IOException() throws Exception {
        File file = new File("c:/Temp/does-not-exist.dat");

        printRun("testOnSubscribeFromFile_IOException", () -> {
            OnSubscribeFromFile onSubscribe = new OnSubscribeFromFile(file, COMPUTATION_SCHEDULER);

            onSubscribe.call(subscriber);
            subscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            subscriber.assertNoValues();
            subscriber.assertError(NoSuchFileException.class);
        });
    }

    private File createFile(List<String> lines) throws IOException {
        File file = Files.createTempFile(null, null).toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), lines);
        return file;
    }
}
