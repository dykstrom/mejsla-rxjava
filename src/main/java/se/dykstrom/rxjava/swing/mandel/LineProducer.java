package se.dykstrom.rxjava.swing.mandel;

import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BackpressureUtils;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * A {@link Producer} class that produces the lines to be emitted by the LineObservable.
 * This class supports backpressure.
 *
 * Is this the easiest or most efficient way to produce and emit the lines that make up
 * an image segment? Definitely not. Much easier would be to create all lines upfront,
 * store them in an array, and use one of the Observable factory methods. But the point
 * of this exercise was to try out backpressure support...
 *
 * @author Johan Dykstrom
 */
class LineProducer implements Producer {

    private static final Logger TLOG = Logger.getLogger(LineProducer.class.getName());

    private static final int NUM_ITERATIONS = 100;

    /** The RGB colors to use when drawing the image. */
    private static final int[] COLORS = new int[256 * 2];

    /** The factor used to convert the "escape time" value to an RGB color. */
    private static final double FACTOR = (double) (COLORS.length - 1) / NUM_ITERATIONS;

    static {
        int index = 0;
        for (int red = 0; red < 256; red++) {
            COLORS[index++] = (new Color(red, 0, 0)).getRGB();
        }
        for (int green = 0; green < 256; green++) {
            COLORS[index++] = (new Color(255, green, 0)).getRGB();
        }
    }

    private final Parameters parameters;
    private final Subscriber<? super Line> subscriber;

    /** The number of requested lines. */
    private final AtomicLong requested = new AtomicLong(0);
    /** The Y value (line number) to use in next request. */
    private final AtomicInteger nextY = new AtomicInteger(0);

    public LineProducer(Parameters parameters, Subscriber<? super Line> subscriber) {
        TLOG.info("Creating producer from parameters " + parameters + " on thread " + Thread.currentThread().getName());
        this.parameters = parameters;
        this.subscriber = subscriber;
    }

    @Override
    public void request(long n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        } else if (n == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)) {
            // MAX_VALUE means that no backpressure is needed
            fastPath();
        } else if (requested.get() != Long.MAX_VALUE) {
            if (BackpressureUtils.getAndAddRequest(requested, n) == 0L) {
                // Backpressure is requested
                slowPath(n);
            }
        }
    }

    private void fastPath() {
        for (int y = 0; y < parameters.getHeight(); y++) {
            if (!subscriber.isUnsubscribed()) produceLine(y);
        }
        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
    }

    private void slowPath(long r) {
        int height = parameters.getHeight();
        while (true) {
            // Get y to start with this time, and increase with number of requested for next time
            int y = nextY.getAndAdd((int) r);

            boolean complete = y < height && nextY.get() >= height;

            for (int count = 0; count < r && y < height; count++, y++) {
                produceLine(y);
            }

            if (complete) {
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            }

            // Now we have produced the number of requested lines, so reduce 'requested' with that
            // At the same time get any new number of requested lines for the next iteration
            r = requested.addAndGet(-r);
            if (r <= 0) {
                return;
            }
        }
    }

    /**
     * Produces a single line, and emits it to the subscriber.
     *
     * @param y The line number of the line to produce.
     */
    private void produceLine(int y) {
        Coordinates coordinates = parameters.getImageAttributes().getCoordinates();
        double scale = parameters.getImageAttributes().getScale();

        final int[] rgb = new int[parameters.getWidth()];
        for (int x = 0; x < rgb.length; x++) {
            int escapeTime = NUM_ITERATIONS - calc(coordinates.getMinX() + x * scale, coordinates.getMinY() + y * scale);
            rgb[x] = COLORS[(int) (escapeTime * FACTOR)];
        }
        if (!subscriber.isUnsubscribed()) subscriber.onNext(new Line(y + parameters.getFirstY(), rgb));
    }

    /**
     * Returns the "escape time" for the given point, that is, the number of iterations it takes
     * before the point reaches the escape condition. A point that does not reach the escape
     * condition within "the maximum number of iterations" is said to belong to the Mandelbrot set.
     * See also <a href="http://en.wikipedia.org/wiki/Mandelbrot_set">Wikipedia</a>.
     *
     * @param x0 The X start value.
     * @param y0 The Y start value.
     * @return The "escape time" of the given point.
     */
    private int calc(double x0, double y0) {
        double x = x0;
        double y = y0;

        int iteration = 0;

        while ((x * x + y * y <= (2 * 2)) && (iteration < NUM_ITERATIONS)) {
            double tempX = x * x - y * y + x0;
            y = 2 * x * y + y0;
            x = tempX;
            iteration++;
        }

        return iteration;
    }
}
