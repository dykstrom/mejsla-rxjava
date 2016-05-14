package se.dykstrom.rxjava.swing.mandel;

import org.junit.Before;
import org.junit.Test;
import rx.Producer;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestLineProducer {

    private static final Parameters PARAMS_WITH_HEIGHT_10 = new Parameters(0, 100, 10, Coordinates.START);
    private static final Parameters PARAMS_WITH_HEIGHT_1000 = new Parameters(0, 100, 1000, Coordinates.START);

    private TestSubscriber<Line> height10Subscriber;
    private TestSubscriber<Line> height1000Subscriber;

    private LineProducer height10Producer;
    private LineProducer height1000Producer;

    @Before
    public void setUp() {
        height10Subscriber = new TestSubscriber<>();
        height10Producer = new LineProducer(PARAMS_WITH_HEIGHT_10, height10Subscriber);
        height1000Subscriber = new TestSubscriber<>();
        height1000Producer = new LineProducer(PARAMS_WITH_HEIGHT_1000, height1000Subscriber);
    }

    @Test
    public void testRequestZero() {
        printRun("testRequestZero", () -> {
            height10Producer.request(0);

            assertEquals(0, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertNotCompleted();
            height10Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestFewer() {
        printRun("testRequestFewer", () -> {
            height10Producer.request(5);

            assertEquals(5, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertNotCompleted();
            height10Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestExact() {
        printRun("testRequestExact", () -> {
            height10Producer.request(10);

            assertEquals(10, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertCompleted();
            height10Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestMore() {
        printRun("testRequestMore", () -> {
            height10Producer.request(20);

            assertEquals(10, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertCompleted();
            height10Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestTwoChunks() {
        printRun("testRequestTwoChunks", () -> {
            height10Producer.request(5);

            assertEquals(5, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertNotCompleted();
            height10Subscriber.assertNoErrors();

            height10Producer.request(5);

            assertEquals(10, height10Subscriber.getOnNextEvents().size());
            height10Subscriber.assertCompleted();
            height10Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestManyChunks() {
        printRun("testRequestManyChunks", () -> {
            for (int i = 0; i < 100; i++) {
                height1000Producer.request(10);
            }

            assertEquals(1000, height1000Subscriber.getOnNextEvents().size());
            height1000Subscriber.assertCompleted();
            height1000Subscriber.assertNoErrors();
        });
    }

    @Test
    public void testRequestNestedChunks() {
        AtomicBoolean completed = new AtomicBoolean(false);
        List<Line> lines = new ArrayList<>();

        printRun("testRequestNestedChunks", () -> {
            NestedSubscriber subscriber = new NestedSubscriber(completed, lines);
            LineProducer producer = new LineProducer(PARAMS_WITH_HEIGHT_1000, subscriber);
            subscriber.setProducer(producer);

            producer.request(100);

            assertEquals(1000, lines.size());
            assertTrue(completed.get());
        });
    }

    // -----------------------------------------------------------------------

    /**
     * A subscriber that makes nested request calls to its producer, similar to flatMap (?)
     */
    private static class NestedSubscriber extends Subscriber<Line> {

        private final AtomicBoolean completed;
        private final List<Line> lines;

        public Producer producer;

        public NestedSubscriber(AtomicBoolean completed, List<Line> lines) {
            this.completed = completed;
            this.lines = lines;
        }

        @Override
        public void setProducer(Producer producer) {
            // Do not call super, since that triggers a request
            this.producer = producer;
        }

        @Override
        public void onCompleted() {
            if (completed.getAndSet(true)) {
                fail("onCompleted called twice");
            }
        }

        @Override
        public void onError(Throwable e) {
            fail("onError called: " + e);
        }

        @Override
        public void onNext(Line line) {
            lines.add(line);
            if (lines.size() % 100 == 0) {
                producer.request(100);
            }
        }
    }
}
