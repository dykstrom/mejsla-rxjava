package se.dykstrom.rxjava.swing.mandel;

import rx.Producer;
import rx.Subscriber;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * A {@link Producer} class that produces the lines to be emitted by the LineObservable.
 * This class supports backpressure.
 *
 * @author Johan Dykstrom
 */
class LineProducer implements Producer {

    private static final Logger TLOG = Logger.getLogger(LineProducer.class.getName());

    private static final int NUM_ITERATIONS = 100;

    /**
     * The RGB colors to use when drawing the image.
     */
    private static final int[] COLORS = new int[256 * 2];

    /**
     * The factor used to convert the "escape time" value to an RGB color.
     */
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

    private final double minX;
    private final double minY;
    private final double dx;
    private final double dy;

    private final AtomicInteger totalY = new AtomicInteger(0);
    private final AtomicInteger requests = new AtomicInteger(0);

    public LineProducer(Parameters parameters, Subscriber<? super Line> subscriber) {
        TLOG.finest("Creating producer from parameters " + parameters + " on thread " + Thread.currentThread().getName());

        this.parameters = parameters;
        this.subscriber = subscriber;

        minX = parameters.getCoordinates().getMinX();
        minY = parameters.getCoordinates().getMinY();
        double maxX = parameters.getCoordinates().getMaxX();
        double maxY = parameters.getCoordinates().getMaxY();

        dx = (maxX - minX) / parameters.getWidth();
        dy = (maxY - minY) / parameters.getHeight();
    }

    @Override
    public void request(long n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        } else if (n == Long.MAX_VALUE) {
            // MAX_VALUE means that no backpressure is needed
            produceLines(parameters.getHeight(), getStartLine(parameters.getHeight()));
            subscriber.onCompleted();
        } else {
            try {
                // Requests can be nested, so we must keep track of how many requests are active
                addRequest();
                produceLines(n, getStartLine((int) n));
            } finally {
                removeRequest();
                // If this is the last request ending, and all lines have been produced,
                // we can safely invoke onCompleted
                if (isLastRequest() && areAllLinesProduced()) {
                    subscriber.onCompleted();
                }
            }
        }
    }

    private void addRequest() {
        requests.incrementAndGet();
    }

    private void removeRequest() {
        requests.decrementAndGet();
    }

    private boolean isLastRequest() {
        return requests.get() == 0;
    }

    private int getStartLine(int n) {
        return totalY.getAndAdd(n);
    }

    private boolean areAllLinesProduced() {
        return totalY.get() >= parameters.getHeight();
    }

    /**
     * Produces {@code n} more lines, starting from line {@code y}.
     */
    private void produceLines(long n, int y) {
        for (int count = 0; count < n && y < parameters.getHeight(); count++, y++) {
            final int[] rgb = new int[parameters.getWidth()];
            for (int x = 0; x < rgb.length; x++) {
                int escapeTime = NUM_ITERATIONS - calc(minX + x * dx, minY + y * dy);
                rgb[x] = COLORS[(int) (escapeTime * FACTOR)];
            }
            subscriber.onNext(new Line(y + parameters.getStartY(), rgb));
        }
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
