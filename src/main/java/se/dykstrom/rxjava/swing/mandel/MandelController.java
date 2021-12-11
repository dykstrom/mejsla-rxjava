package se.dykstrom.rxjava.swing.mandel;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;

class MandelController {

    private static final Logger TLOG = Logger.getLogger(MandelController.class.getName());

    private final MandelView view;

    /** The number of segments to divide the image into. */
    private final int segments;

    /** Stack used to store undo objects, that is, {@code ImageAttributes} objects.  */
    private final Stack<ImageAttributes> undoStack = new Stack<>();

    public MandelController(MandelView view, int segments) {
        this.view = view;
        this.segments = segments;
        undoStack.push(ImageAttributes.INITIAL_ATTRIBUTES);
        initSubscriptions();
    }

    private void initSubscriptions() {
        // Window resize events
        view.resizeObs()
                .debounce(150, TimeUnit.MILLISECONDS)
                .subscribe(this::resizeAction);

        // Rubber band selection events
        view.rubberBandObs().subscribe(this::rubberBandAction);

        // Menu selection events
        view.newActionObs().subscribe(event -> newAction());
        view.exitActionObs().subscribe(event -> exitAction());
        view.undoActionObs().subscribe(event -> undoAction());
    }

    // --- Actions ---

    private void newAction() {
        Dimension size = view.getImageSize();
        double scale;
        Rectangle bounds;
        if (size.getWidth() > size.getHeight()) {
            scale = Coordinates.INITIAL_SIZE / size.getHeight();
            bounds = new Rectangle(0, 0, (int) size.getHeight(), (int) size.getHeight());
        } else {
            scale = Coordinates.INITIAL_SIZE / size.getWidth();
            bounds = new Rectangle(0, 0, (int) size.getWidth(), (int) size.getWidth());
        }
        Coordinates coordinates = centerImage(size, bounds, scale,
                Coordinates.INITIAL_COORDINATES.minX(), Coordinates.INITIAL_COORDINATES.minY());
        undoStack.push(createImage(new ImageAttributes(coordinates, scale)));
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

    private void resizeAction(Dimension dimension) {
        createImage(undoStack.peek());
    }

    private void rubberBandAction(RubberBandSelectionEvent event) {
        Rectangle bounds = event.getSelectionBounds();
        TLOG.finest("Selected area = " + bounds);

        ImageAttributes imageAttributes = undoStack.peek();
        Coordinates coordinates = imageAttributes.coordinates();
        final double scale = imageAttributes.scale();

        final double newMinX = coordinates.minX() + bounds.getX() * scale;
        final double newMinY = coordinates.minY() + bounds.getY() * scale;
        final double newScale = calculateNewScale(view.getImageSize(), bounds, scale);
        final Coordinates newCoordinates = centerImage(view.getImageSize(), bounds, newScale, newMinX, newMinY);

        undoStack.push(createImage(new ImageAttributes(newCoordinates, newScale)));
    }

    /**
     * Calculates new coordinates that center the image after zooming in on a selected area.
     * The intention is to position the selected area in the middle of the new image.
     *
     * @param size The size of the image in pixels.
     * @param bounds The bounds of the selected area in pixels.
     * @param scale The new scale after zooming in.
     * @param minX The new min X after zooming in.
     * @param minY The new min Y after zooming in.
     * @return The coordinates for a centered image.
     */
    private Coordinates centerImage(Dimension size, Rectangle bounds, double scale, double minX, double minY) {
        final double x1 = size.getWidth();
        final double x2 = bounds.getWidth();
        final double y1 = size.getHeight();
        final double y2 = bounds.getHeight();

        if (y2 / y1 > x2 / x1) {
            // Calculate the width of the selected area after zooming in
            double x3 = y1 / y2 * x2;
            double pixelsLeftOfArea = (x1 - x3) / 2;
            return new Coordinates(minX - pixelsLeftOfArea * scale, minY);
        } else {
            // Calculate the height of the selected area after zooming in
            double y3 = x1 / x2 * y2;
            double pixelsAboveArea = (y1 - y3) / 2;
            return new Coordinates(minX, minY - pixelsAboveArea * scale);
        }
    }

    /**
     * Calculates a new scale after zooming in on a selected area. The new scale is calculated from
     * the relation between the image size (size) and the selected area (bounds), and the old scale.
     *
     * @param size The size of the image in pixels.
     * @param bounds The bounds of the selected area in pixels.
     * @param scale The old scale.
     * @return The new scale.
     */
    private double calculateNewScale(Dimension size, Rectangle bounds, double scale) {
        final double x1 = size.getWidth();
        final double x2 = bounds.getWidth();
        final double y1 = size.getHeight();
        final double y2 = bounds.getHeight();
        if (y2 / y1 > x2 / x1) {
            return y2 / y1 * scale;
        } else {
            return x2 / x1 * scale;
        }
    }

    /**
     * Creates a new fractal image that fits the current size of the image panel.
     *
     * @param imageAttributes The image attributes that defines the image to create.
     * @return The actual coordinates use the draw the image.
     */
    private ImageAttributes createImage(final ImageAttributes imageAttributes) {
        Dimension imageSize = view.getImageSize();
        int width = (int) imageSize.getWidth();
        int height = (int) imageSize.getHeight();

        Observable<Line> lineObs = paramObs(segments, width, height, imageAttributes)
                .flatMap(params -> LineObservable.fromParameters(params)
                        .subscribeOn(Schedulers.computation()));
        view.drawImage(lineObs);

        return imageAttributes;
    }

    /**
     * Returns an Observable that emits one Parameters object per image segment to draw.
     */
    private static Observable<Parameters> paramObs(int segments, int width, int height, ImageAttributes imageAttributes) {
        final int linesPerSegment = height / segments;
        TLOG.info("Number of lines = " + height + ", lines per segment = " + linesPerSegment);

        // Divide the coordinate space among the segments
        Coordinates coordinates = imageAttributes.coordinates();
        double coordinatesPerSegment = height * imageAttributes.scale() / segments;

        List<Parameters> parametersList = new ArrayList<>();

        // Create parameters for all segments
        for (int s = 0; s < (segments - 1); s++) {
            double minY = coordinates.minY() + (coordinatesPerSegment * s);
            ImageAttributes segmentAttributes = imageAttributes.withCoordinates(coordinates.withMinY(minY));
            parametersList.add(new Parameters(linesPerSegment * s, width, linesPerSegment, segmentAttributes));
        }

        // Assign the rest of the lines to the last segment
        double minY = coordinates.minY() + (coordinatesPerSegment * (segments - 1));
        ImageAttributes segmentAttributes = imageAttributes.withCoordinates(coordinates.withMinY(minY));
        int segmentHeight = height - ((segments - 1) * linesPerSegment);
        parametersList.add(new Parameters(linesPerSegment * (segments - 1), width, segmentHeight, segmentAttributes));

        return Observable.from(parametersList);
    }
}
