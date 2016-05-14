package se.dykstrom.rxjava.swing.mandel;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class MandelController {

    private static final Logger TLOG = Logger.getLogger(MandelController.class.getName());

    private final MandelView view;

    private final int segments;

    /** Stack used to store undo objects, that is, {@code Coordinates} objects.  */
    private final Stack<Coordinates> undoStack = new Stack<Coordinates>() { { push(Coordinates.START); } };

    public MandelController(MandelView view, int segments) {
        this.view = view;
        this.segments = segments;
        initSubscriptions();
    }

    private void initSubscriptions() {
        // Window resize events
        view.resizeObs()
                .debounce(150, TimeUnit.MILLISECONDS)
                .subscribe(dimension -> createImage(undoStack.peek()));

        // Rubber band selection events
        view.rubberBandObs().subscribe(this::rubberBandAction);

        // Menu selection events
        view.newActionObs().subscribe(event -> newAction());
        view.exitActionObs().subscribe(event -> exitAction());
        view.undoActionObs().subscribe(event -> undoAction());
    }

    // --- Actions ---

    private void newAction() {
        undoStack.push(createImage(Coordinates.START));
    }

    private void exitAction() {
        System.exit(0);
    }

    private void undoAction() {
        if (undoStack.size() > 1) {
            // Throw away the top item, since that was used to create the current image
            undoStack.pop();

            // Use next item to create a new image, identical to the previous image
            createImage(undoStack.peek());
        }
    }

    private void rubberBandAction(RubberBandSelectionEvent event) {
        Rectangle bounds = event.getSelectionBounds();
        TLOG.finest("Selected area = " + bounds);

        Coordinates coordinates = undoStack.peek();
        Dimension size = view.getImageSize();

        final double widthPerPixel = (coordinates.getMaxX() - coordinates.getMinX()) / size.getWidth();
        final double heightPerPixel = (coordinates.getMaxY() - coordinates.getMinY()) / size.getHeight();

        final double newMinX = coordinates.getMinX() + bounds.getX() * widthPerPixel;
        final double newMaxX = coordinates.getMinX() + (bounds.getX() + bounds.getWidth() + 1) * widthPerPixel;
        final double newMinY = coordinates.getMinY() + bounds.getY() * heightPerPixel;
        final double newMaxY = coordinates.getMinY() + (bounds.getY() + bounds.getHeight() + 1) * heightPerPixel;

        undoStack.push(createImage(new Coordinates(newMinX, newMaxX, newMinY, newMaxY)));
    }

    /**
     * Creates a new fractal image that fits the current size of the image panel.
     * Fixes the aspect ratio if this is a zoomed in image.
     *
     * @param coordinates The coordinates that bound the image to draw.
     * @return The actual coordinates use the draw the image.
     */
    private Coordinates createImage(Coordinates coordinates) {
        int width = (int) view.getImageSize().getWidth();
        int height = (int) view.getImageSize().getHeight();

        // Adjust the coordinates to make the image look right
        coordinates = fixAspectRatio(coordinates, width, height);

        Observable<Line> lineObs = paramObs(segments, width, height, coordinates)
                .flatMap(params -> LineObservable.fromParameters(params)
                        .subscribeOn(Schedulers.computation()));
        view.drawImage(lineObs);

        return coordinates;
    }

    /**
     * Returns an Observable that emits one Parameters object per image segment to draw.
     */
    private static Observable<Parameters> paramObs(int segments, int width, int height, Coordinates coordinates) {
        final int linesPerSegment = height / segments;
        TLOG.info("Number of lines = " + height + ", lines per segment = " + linesPerSegment);

        // Divide the coordinate space among the segments
        double coordinatesPerSegment = (coordinates.getMaxY() - coordinates.getMinY()) / segments;

        List<Parameters> parametersList = new ArrayList<>();

        // Create parameters for all segments
        for (int s = 0; s < (segments - 1); s++) {
            double minY = coordinates.getMinY() + (coordinatesPerSegment * s);
            double maxY = minY + coordinatesPerSegment;
            parametersList.add(new Parameters(linesPerSegment * s, width, linesPerSegment, new Coordinates(coordinates.getMinX(), coordinates.getMaxX(), minY, maxY)));
        }

        // Assign the rest of the lines to the last section
        double minY = coordinates.getMinY() + (coordinatesPerSegment * (segments - 1));
        double maxY = minY + coordinatesPerSegment;
        parametersList.add(new Parameters(linesPerSegment * (segments - 1), width, height - ((segments - 1) * linesPerSegment), new Coordinates(coordinates.getMinX(), coordinates.getMaxX(), minY, maxY)));

        return Observable.from(parametersList);
    }

    /**
     * Adjusts the Y coordinates to preserve the aspect ratio.
     *
     * @param coordinates The image coordinates.
     * @param width  The image width in pixels.
     * @param height The image height in pixels.
     */
    private static Coordinates fixAspectRatio(Coordinates coordinates, int width, int height) {
        double maxX = coordinates.getMaxX();
        double minX = coordinates.getMinX();
        double maxY = coordinates.getMaxY();
        double minY = coordinates.getMinY();

        double diffX = maxX - minX;
        double diffY = maxY - minY;

        double ratio = (double) height / (double) width;

        double newDiffY = diffX * ratio;

        double yDiffDiff = newDiffY - diffY;
        double newMinY = minY - yDiffDiff / 2;
        double newMaxY = maxY + yDiffDiff / 2;

        return new Coordinates(minX, maxX, newMinY, newMaxY);
    }
}
