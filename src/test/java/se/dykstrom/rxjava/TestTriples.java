package se.dykstrom.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.dykstrom.rxjava.Utils.timeRun;

public class TestTriples {

    private static final List<Triples.Triple> TRIPLES = Arrays.asList(
            new Triples.Triple(3, 4, 5),
            new Triples.Triple(5, 12, 13),
            new Triples.Triple(8, 15, 17),
            new Triples.Triple(7, 24, 25),
            new Triples.Triple(20, 21, 29),
            new Triples.Triple(12, 35, 37),
            new Triples.Triple(9, 40, 41),
            new Triples.Triple(161, 240, 289)
    );
    
    private final TestSubscriber<Triples.Triple> testSubscriber = new TestSubscriber<>();

    @Test
    public void testIsCoPrime() {
        assertTrue(new Triples.Triple(3, 4, 5).coprime());
        assertTrue(new Triples.Triple(5, 12, 13).coprime());
        assertTrue(new Triples.Triple(8, 15, 17).coprime());

        assertFalse(new Triples.Triple(6, 8, 10).coprime());
        assertFalse(new Triples.Triple(9, 12, 15).coprime());
        assertFalse(new Triples.Triple(10, 24, 26).coprime());
    }

    @Test
    public void testTriples() {
        long time = timeRun(() -> {
            Observable<Integer> integers = Observable.range(1, 300);
            Observable<Triples.Triple> triples = Triples.triples(integers);
            Observable<Triples.Triple> primitive = Triples.primitive(triples);

            primitive.subscribe(testSubscriber);

            List<Triples.Triple> onNextEvents = testSubscriber.getOnNextEvents();
            onNextEvents.stream().forEach(System.out::println);

            assertTrue(onNextEvents.containsAll(TRIPLES));

            testSubscriber.assertCompleted();
        });
        System.out.printf("[%s] finished after %d ms\n", "testTriples", time);
    }
}
