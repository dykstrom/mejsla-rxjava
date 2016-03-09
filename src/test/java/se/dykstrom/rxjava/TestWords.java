package se.dykstrom.rxjava;

import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestWords {

    private static final String FILENAME = "test.txt";

    private TestSubscriber<List<Words.WordCount>> testSubscriber;

    @Before
    public void setUp() {
        testSubscriber = new TestSubscriber<>();
    }

    @Test
    public void testWordsByCount() {
        long time = Utils.timeRun(() -> {
            List<String> input = Arrays.asList(
                    "one two three four five",
                    "two three four five",
                    "three four five",
                    "four five",
                    "five");
            List<Words.WordCount> expected = Arrays.asList(
                    new Words.WordCount("five", 5),
                    new Words.WordCount("four", 4),
                    new Words.WordCount("three", 3),
                    new Words.WordCount("two", 2),
                    new Words.WordCount("one", 1));

            Observable<String> strings = Observable.from(input);
            Observable<List<Words.WordCount>> wordsByCount = Words.wordsByCount(strings);

            wordsByCount.subscribe(testSubscriber);

            List<List<Words.WordCount>> onNextEvents = testSubscriber.getOnNextEvents();
            assertEquals(1, onNextEvents.size());

            List<Words.WordCount> actual = onNextEvents.get(0);
            assertEquals(expected, actual);

            testSubscriber.assertCompleted();
        });
        System.out.printf("[%s] finished after %d ms\n", "testWordsByCount", time);
    }

    @Test
    public void analyzeFile() {
        Observable<String> strings = Utils.fromFile(new File(FILENAME));
        Observable<List<Words.WordCount>> wordsByCount = Words.wordsByCount(strings);

        System.out.printf("Count   Word\n");
        System.out.printf("-----   ---------------------\n");
        wordsByCount.subscribe(
                list -> list.stream().forEach(wc -> System.out.printf("%5d   %s\n", wc.getValue(), wc.getKey())),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.printf("-----   ---------------------\n")
        );
    }
}
