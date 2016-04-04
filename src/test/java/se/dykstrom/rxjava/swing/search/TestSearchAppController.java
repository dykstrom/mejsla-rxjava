package se.dykstrom.rxjava.swing.search;

import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import se.dykstrom.rxjava.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSearchAppController {

    private static final String SOME_RESULTS_JSON =
            "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":2547},\"search\":" +
            "[{\"ns\":0,\"title\":\"Katt\"},{\"ns\":0,\"title\":\"Klas Katt\"}]}}";

    private static final String OTHER_RESULTS_JSON =
            "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":2547},\"search\":" +
            "[{\"ns\":0,\"title\":\"Java\"}]}}";

    private final TestSubscriber<String> stringSubscriber = new TestSubscriber<>();

    private final TestSubscriber<List<String>> listSubscriber = new TestSubscriber<>();

    @Test
    public void testDocumentFromTitle() throws Exception {
        String document = "some text";
        Func1<URL, Observable<String>> observableFactory = url -> Observable.just(document);

        test("testDocumentFromTitle", () -> {
            Observable<String> source = Observable.zip(
                    Observable.just("foo", "bar"),
                    Observable.interval(100, TimeUnit.MILLISECONDS),
                    (x, y) -> x);
            Observable<String> observable = SearchAppController.documentFromTitleObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = asList(document, document);
            List<String> actual = stringSubscriber.getOnNextEvents();
            assertEquals(expected, actual);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromTitle_NoTitle() throws Exception {
        test("testDocumentFromTitle_NoTitle", () -> {
            Observable<String> source = Observable.just("");
            Observable<String> observable = SearchAppController.documentFromTitleObs(source);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = singletonList(SearchAppController.NO_DOCUMENT);
            List<String> actual = stringSubscriber.getOnNextEvents();
            assertEquals(expected, actual);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromTitle_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        test("testDocumentFromTitle_IOException", () -> {
            Observable<String> source = Observable.just("foo");
            Observable<String> observable = SearchAppController.documentFromTitleObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> actual = stringSubscriber.getOnNextEvents();
            assertTrue(actual.isEmpty());
            stringSubscriber.assertError(IOException.class);
        });
    }

    @Test
    public void testDocumentFromLink() throws Exception {
        String document0 = "some text";
        String document1 = "some other text";
        Func1<URL, Observable<String>> observableFactory = url -> Observable.just(document0, document1);

        URL url = new URL("http://www.google.com");
        test("testDocumentFromLink", () -> {
            Observable<URL> source = Observable.just(url);
            Observable<String> observable = SearchAppController.documentFromLinkObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = asList(document0, document1);
            List<String> actual = stringSubscriber.getOnNextEvents();
            assertEquals(expected, actual);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromLink_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        URL url = new URL("http://www.google.com");
        test("testDocumentFromLink_IOException", () -> {
            Observable<URL> source = Observable.just(url);
            Observable<String> observable = SearchAppController.documentFromLinkObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> actual = stringSubscriber.getOnNextEvents();
            assertTrue(actual.isEmpty());
            stringSubscriber.assertError(IOException.class);
        });
    }

    @Test
    public void testTitlesFromSearchText() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.just(url.getQuery().contains("foo") ? SOME_RESULTS_JSON : OTHER_RESULTS_JSON);

        test("testTitlesFromSearchText", () -> {
            Observable<String> source0 = Observable.just("f", "foo");
            Observable<String> source1 = Observable.zip(
                    Observable.just("bar"),
                    Observable.timer(1200, TimeUnit.MILLISECONDS),
                    (x, y) -> x);
            Observable<String> source2 = Observable.<String>empty().delay(1200, TimeUnit.MILLISECONDS);

            // The source will start by emitting "f" and "foo" almost immediately
            // After a delay of 1.2 seconds it will emit "bar"
            // After a second delay of 1.2 seconds, it will complete
            Observable<String> source = Observable.concat(source0, source1, source2);
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source, observableFactory);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(3000, TimeUnit.MILLISECONDS);

            List<List<String>> expected = asList(asList("Katt", "Klas Katt"), singletonList("Java"));
            List<List<String>> actual = listSubscriber.getOnNextEvents();
            assertEquals(expected, actual);
            listSubscriber.assertCompleted();
        });
    }

    @Test
    public void testTitlesFromSearchText_NoText() throws Exception {
        test("testTitlesFromSearchText_NoText", () -> {
            Observable<String> source = Observable.just("");
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<List<String>> expected = singletonList(SearchAppController.NO_RESULTS);
            List<List<String>> actual = listSubscriber.getOnNextEvents();
            assertEquals(expected, actual);
            listSubscriber.assertCompleted();
        });
    }

    @Test
    public void testTitlesFromSearchText_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        test("testTitlesFromSearchText_IOException", () -> {
            Observable<String> source = Observable.just("foo");
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source, observableFactory);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<List<String>> actual = listSubscriber.getOnNextEvents();
            assertTrue(actual.isEmpty());
            listSubscriber.assertError(IOException.class);
        });
    }

    private static void test(String name, Runnable runnable) {
        long time = Utils.timeRun(runnable);
        System.out.printf("[%s] finished after %d ms\n", name, time);
    }
}
