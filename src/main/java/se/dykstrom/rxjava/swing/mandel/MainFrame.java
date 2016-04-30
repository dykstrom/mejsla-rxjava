package se.dykstrom.rxjava.swing.mandel;

import rx.Observable;
import rx.observables.SwingObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;
import se.dykstrom.rxjava.swing.components.RubberBandSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Creates the application main frame, including all the components visible in
 * the main frame. This class also handles menu item callbacks, rubber band
 * selection callbacks, and contains application logic for saving, opening, and
 * creating new fractal images.
 *
 * @author Johan Dykstrom
 */
class MainFrame extends JFrame {

    private static Logger TLOG = Logger.getLogger(MainFrame.class.getName());

    private final int workers;

    private MandelPanel mandelPanel;

    /**
     * The parameter bean used to transfer image parameters between components.
     */
    private Coordinates coordinates;

    /**
     * Stack used to store undo objects, that is {@code Coordinates} objects.
     */
    private final Stack<Coordinates> undoStack = new Stack<>();

    /**
     * Creates new form MainFrame
     *
     * @param workers Number of workers.
     */
    MainFrame(int workers) {
        this.workers = workers;
        this.coordinates = new Coordinates();
        initComponents();
        initSubscriptions();
    }

    private void initSubscriptions() {
        SwingObservable.fromResizing(this)
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribe(dimension -> createImage());
    }

    private void initComponents() {
        mandelPanel = new MandelPanel();

        RubberBandSelector rubberBandSelector = new RubberBandSelector(mandelPanel);
        rubberBandSelector.addRubberBandListener(this::rubberBandSelectionPerformed);

        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mandel");
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(700, 700));

        add(mandelPanel, BorderLayout.CENTER);

        pack();
    }

    private JMenuBar createMenuBar() {
        // Menu bar
        JMenu fileMenu = new JMenu();
        fileMenu.setText("File");
        fileMenu.setMnemonic('F');

        JMenuItem newMenuItem = new JMenuItem();
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(this::newMenuItemActionPerformed);
        fileMenu.add(newMenuItem);

        JMenuItem exitMenuItem = new JMenuItem();
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(this::exitMenuItemActionPerformed);
        fileMenu.add(exitMenuItem);

        JMenu editMenu = new JMenu();
        editMenu.setText("Edit");
        editMenu.setMnemonic('E');

        JMenuItem undoMenuItem = new JMenuItem();
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        undoMenuItem.setText("Undo");
        undoMenuItem.addActionListener(this::undoMenuItemActionPerformed);
        editMenu.add(undoMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    private void exitMenuItemActionPerformed(ActionEvent event) {
        System.exit(0);
    }

    private void newMenuItemActionPerformed(ActionEvent event) {
        coordinates = new Coordinates();
        createImage();
    }

    private void undoMenuItemActionPerformed(ActionEvent evt) {
        if (undoStack.size() >= 2) {
            // Throw away the top item, since that was used to create the current image
            undoStack.pop();

            // Use next item to create a new image, identical to the previous image
            coordinates = undoStack.pop();

            createImage();
        }
    }

    private void rubberBandSelectionPerformed(RubberBandSelectionEvent event) {
        TLOG.finest("Selected area = " + event.getSelectionBounds());

        final double widthPerPixel = (coordinates.getMaxX() - coordinates.getMinX()) / mandelPanel.getWidth();
        final double heightPerPixel = (coordinates.getMaxY() - coordinates.getMinY()) / mandelPanel.getHeight();

        final double newMinX = coordinates.getMinX() +
                event.getSelectionBounds().getX() * widthPerPixel;
        final double newMaxX = coordinates.getMinX() +
                (event.getSelectionBounds().getX() + event.getSelectionBounds().getWidth() + 1) * widthPerPixel;
        final double newMinY = coordinates.getMinY() +
                event.getSelectionBounds().getY() * heightPerPixel;
        final double newMaxY = coordinates.getMinY() +
                (event.getSelectionBounds().getY() + event.getSelectionBounds().getHeight() + 1) * heightPerPixel;

        TLOG.finest("Old coordinate space = " + coordinates);
        coordinates = new Coordinates(newMinX, newMaxX, newMinY, newMaxY);
        TLOG.finest("New coordinate space = " + coordinates);

        createImage();
    }

    /**
     * Creates a new fractal image that fits the current size of the mandel
     * panel. Fix the aspect ratio if this is a zoomed in image. Divide the
     * image, and coordinate space, into parts. Assign one part to each worker
     * thread. Start all worker threads in parallel.
     */
    private void createImage() {
        int width = mandelPanel.getWidth();
        int height = mandelPanel.getHeight();

        // Adjust the coordinates to make the image look right
        coordinates = fixAspectRatio(coordinates, width, height);

        Observable<Line> imageObs = paramObs(workers, width, height, coordinates)
                .onBackpressureBuffer(100000)
                .flatMap(params -> CalcObservable.fromParams(params, coordinates)
                        .observeOn(Schedulers.computation()));

        mandelPanel.clear();
        imageObs.observeOn(SwingScheduler.getInstance()).subscribe(
                result -> mandelPanel.draw(result),
                throwable -> {
                    System.err.println("Error: " + throwable);
                    throwable.printStackTrace();
                },
                () -> mandelPanel.finish());

        // Push the parameters we used on the undo stack
        undoStack.push(coordinates);
    }

    private static Observable<Params> paramObs(int numThreads, int width, int height, Coordinates coordinates) {
        final int linesPerThread = height / numThreads;
        TLOG.info("Total number of lines = " + height);
        TLOG.info("Number of workers = " + numThreads);
        TLOG.info("Lines per worker = " + linesPerThread);
        TLOG.info("Coordinate space = " + coordinates);

        // Divide the coordinate space among the workers
        final double coordsPerThread = (coordinates.getMaxY() - coordinates.getMinY()) / numThreads;

        List<Params> paramsList = new ArrayList<>();

        // Create one worker for each thread
        for (int t = 0; t < (numThreads - 1); t++) {
            double yMin = coordinates.getMinY() + (coordsPerThread * t);
            double yMax = yMin + coordsPerThread;
            paramsList.add(new Params(linesPerThread * t, width, linesPerThread, yMin, yMax));
        }

        // Assign the rest of the lines to the last worker
        double yMin = coordinates.getMinY() + (coordsPerThread * (numThreads - 1));
        double yMax = yMin + coordsPerThread;
        paramsList.add(new Params(linesPerThread * (numThreads - 1), width, height - ((numThreads - 1) * linesPerThread), yMin, yMax));

        return Observable.from(paramsList);
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
