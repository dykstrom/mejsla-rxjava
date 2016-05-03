package se.dykstrom.rxjava.swing.search;

import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

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

        printRun("testDocumentFromTitle", () -> {
            Observable<String> source = Observable.just("foo", "bar");
            Observable<String> observable = SearchAppController.documentFromTitleObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = asList(document, document);
            stringSubscriber.assertReceivedOnNext(expected);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromTitle_NoTitle() throws Exception {
        printRun("testDocumentFromTitle_NoTitle", () -> {
            Observable<String> source = Observable.just("");
            Observable<String> observable = SearchAppController.documentFromTitleObs(source);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = singletonList(SearchAppController.NO_DOCUMENT);
            stringSubscriber.assertReceivedOnNext(expected);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromTitle_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        printRun("testDocumentFromTitle_IOException", () -> {
            Observable<String> source = Observable.just("foo");
            Observable<String> observable = SearchAppController.documentFromTitleObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            stringSubscriber.assertNoValues();
            stringSubscriber.assertError(IOException.class);
        });
    }

    @Test
    public void testDocumentFromLink() throws Exception {
        String document0 = "some text";
        String document1 = "some other text";
        Func1<URL, Observable<String>> observableFactory = url -> Observable.just(document0, document1);

        URL url = new URL("http://www.google.com");
        printRun("testDocumentFromLink", () -> {
            Observable<URL> source = Observable.just(url);
            Observable<String> observable = SearchAppController.documentFromLinkObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<String> expected = asList(document0, document1);
            stringSubscriber.assertReceivedOnNext(expected);
            stringSubscriber.assertCompleted();
        });
    }

    @Test
    public void testDocumentFromLink_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        URL url = new URL("http://www.google.com");
        printRun("testDocumentFromLink_IOException", () -> {
            Observable<URL> source = Observable.just(url);
            Observable<String> observable = SearchAppController.documentFromLinkObs(source, observableFactory);

            observable.subscribe(stringSubscriber);
            stringSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            stringSubscriber.assertNoValues();
            stringSubscriber.assertError(IOException.class);
        });
    }

    @Test
    public void testTitlesFromSearchText() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.just(url.getQuery().contains("Katt") ? SOME_RESULTS_JSON : OTHER_RESULTS_JSON);

        printRun("testTitlesFromSearchText", () -> {
            int delay = SearchAppController.TYPE_DELAY * 120 / 100;
            Observable<String> source0 = Observable.just("K", "Katt");
            Observable<String> source1 = Observable.just("Java").delay(delay, TimeUnit.MILLISECONDS);
            Observable<String> source2 = Observable.<String>empty().delay(delay, TimeUnit.MILLISECONDS);

            // The source will start by emitting "K" and "Katt" almost immediately
            // After a delay of 0.6 seconds it will emit "Java"
            // After a second delay of 0.6 seconds, it will complete
            Observable<String> source = Observable.concat(source0, source1, source2);
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source, observableFactory);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(2000, TimeUnit.MILLISECONDS);

            List<List<String>> expected = asList(asList("Katt", "Klas Katt"), singletonList("Java"));
            listSubscriber.assertReceivedOnNext(expected);
            listSubscriber.assertCompleted();
        });
    }

    @Test
    public void testTitlesFromSearchText_NoText() throws Exception {
        printRun("testTitlesFromSearchText_NoText", () -> {
            Observable<String> source = Observable.just("");
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            List<List<String>> expected = singletonList(SearchAppController.NO_RESULTS);
            listSubscriber.assertReceivedOnNext(expected);
            listSubscriber.assertCompleted();
        });
    }

    @Test
    public void testTitlesFromSearchText_IOException() throws Exception {
        Func1<URL, Observable<String>> observableFactory = url -> Observable.error(new IOException());

        printRun("testTitlesFromSearchText_IOException", () -> {
            Observable<String> source = Observable.just("foo");
            Observable<List<String>> observable = SearchAppController.titlesFromSearchTextObs(source, observableFactory);

            observable.subscribe(listSubscriber);
            listSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

            listSubscriber.assertNoValues();
            listSubscriber.assertError(IOException.class);
        });
    }
}
