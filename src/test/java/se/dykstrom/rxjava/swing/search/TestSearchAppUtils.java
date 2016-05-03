package se.dykstrom.rxjava.swing.search;

import com.google.gson.JsonSyntaxException;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSearchAppUtils {

    private static final String SOME_RESULTS_JSON =
            "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":2547},\"search\":" +
            "[" +
            "{\"ns\":0,\"title\":\"Katt\"}," +
            "{\"ns\":0,\"title\":\"Gråbo (katt)\"}," +
            "{\"ns\":0,\"title\":\"Nicky Katt\"}," +
            "{\"ns\":0,\"title\":\"Klas Katt\"}" +
            "]}}";

    private static final String INVALID_RESULTS_JSON =
            "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":2547},\"search\":" +
            "" +
            "{\"ns\":0,\"title\":\"Katt\"}," +
            "]}}";

    private static final String NO_RESULTS_JSON =
            "{\"batchcomplete\":\"\",\"query\":{\"searchinfo\":{\"totalhits\":0},\"search\":[]}}";

    private final TestSubscriber<String> titleSubscriber = new TestSubscriber<>();

    @Test
    public void testTitleObs() throws Exception {
        Collection<String> expected = Arrays.asList("Katt", "Gråbo (katt)", "Nicky Katt", "Klas Katt");
        Collection<String> actual = subscribeToTitleObs(titleSubscriber, SOME_RESULTS_JSON);
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
        titleSubscriber.assertCompleted();
    }

    @Test
    public void testTitleObs_NoResults() throws Exception {
        Collection<String> actual = subscribeToTitleObs(titleSubscriber, NO_RESULTS_JSON);
        assertTrue(actual.isEmpty());
        titleSubscriber.assertCompleted();
    }

    @Test
    public void testTitleObs_InvalidResults() throws Exception {
        Collection<String> actual = subscribeToTitleObs(titleSubscriber, INVALID_RESULTS_JSON);
        assertTrue(actual.isEmpty());
        titleSubscriber.assertError(JsonSyntaxException.class);
    }

    private static Collection<String> subscribeToTitleObs(TestSubscriber<String> subscriber, String json) {
        Observable<String> titleObs = SearchAppUtils.titleObs(json);
        titleObs.subscribe(subscriber);
        return subscriber.getOnNextEvents();
    }
}
