package se.dykstrom.rxjava.swing.mandel;

import rx.Observable;
import rx.Subscriber;

import java.util.logging.Logger;

class CalcObservable {

    private static Logger TLOG = Logger.getLogger(CalcObservable.class.getName());

    private CalcObservable() { }

    static Observable<Line> fromParams(Params params, Coordinates coordinates) {
        return Observable.create(new OnSubscribeFromParams(params, coordinates));
    }

    /**
     * Returns the "escape time" for the given point, i.e. the number of
     * iterations it takes before the point reaches the escape condition.
     * A point that does not reach the escape condition within "the maximum
     * number of iterations" is said to belong to the Mandelbrot set. This
     * function was written using pseudo code for the "escape time" algorithm
     * from a Wikipedia article about the
     * <a href="http://en.wikipedia.org/wiki/Mandelbrot_set" target="_blank">
     * Mandelbrot set.</a>
     *
     * @param x0 The X start value.
     * @param y0 The Y start value.
     * @return The "escape time" of the given point.
     */
    private static int calc(double x0, double y0) {
        double x = x0;
        double y = y0;

        int iteration = 0;
        int maxIterations = Params.NUM_ITERATIONS;

        while ((x * x + y * y <= (2 * 2)) && (iteration < maxIterations)) {
            double tempX = x * x - y * y + x0;
            y = 2 * x * y + y0;
            x = tempX;
            iteration++;
        }

        return iteration;
    }

    private static class OnSubscribeFromParams implements Observable.OnSubscribe<Line> {

        private final Params params;
        private final Coordinates coordinates;

        OnSubscribeFromParams(Params params, Coordinates coordinates) {
            this.params = params;
            this.coordinates = coordinates;
        }

        @Override
        public void call(Subscriber<? super Line> subscriber) {
            TLOG.info("Creating obs from params " + params + " and coords " + coordinates + " on thread " + Thread.currentThread().getName());

            final double dy = (params.getMaxY() - params.getMinY()) / params.getHeight();
            final double dx = (coordinates.getMaxX() - coordinates.getMinX()) / params.getWidth();

            for (int y = 0; y < params.getHeight(); y++) {
                final int[] colors = new int[params.getWidth()];
                for (int x = 0; x < params.getWidth(); x++) {
                    colors[x] = Params.NUM_ITERATIONS - calc(coordinates.getMinX() + x * dx, params.getMinY() + y * dy);
                }
                subscriber.onNext(new Line(y + params.getFirstLine(), colors));
            }
            subscriber.onCompleted();
        }
    }
}
