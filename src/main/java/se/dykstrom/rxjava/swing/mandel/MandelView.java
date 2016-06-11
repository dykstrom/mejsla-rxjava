package se.dykstrom.rxjava.swing.mandel;

import rx.Observable;
import rx.observables.SwingObservable;
import rx.schedulers.SwingScheduler;
import se.dykstrom.rxjava.swing.common.SwingObservables;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Creates the main view and all its components.
 *
 * @author Johan Dykstrom
 */
class MandelView extends JFrame {

    private MandelPanel mandelPanel;

    private JMenuItem exitMenuItem;
    private JMenuItem newMenuItem;
    private JMenuItem undoMenuItem;

    MandelView() {
        initComponents();
    }

    // --- Exposed observables ---

    /**
     * Returns an Observable that emits Dimension objects resulting from resizing the window.
     */
    Observable<Dimension> resizeObs() { return SwingObservable.fromResizing(this); }

    /**
     * Returns an Observable that emits selection events from the image panel.
     */
    Observable<RubberBandSelectionEvent> rubberBandObs() {
        return SwingObservables.fromRubberBandSelectionEvents(mandelPanel);
    }

    /**
     * Returns an Observable that emits action events resulting from menu selections.
     */
    Observable<ActionEvent> newActionObs() {
        return SwingObservable.fromButtonAction(newMenuItem);
    }

    /**
     * Returns an Observable that emits action events resulting from menu selections.
     */
    Observable<ActionEvent> exitActionObs() {
        return SwingObservable.fromButtonAction(exitMenuItem);
    }

    /**
     * Returns an Observable that emits action events resulting from menu selections.
     */
    Observable<ActionEvent> undoActionObs() {
        return SwingObservable.fromButtonAction(undoMenuItem);
    }

    /**
     * Returns the size of the image as a Dimension object.
     */
    Dimension getImageSize() {
        return mandelPanel.getSize();
    }

    /**
     * Draws a new fractal image using the image line data emitted by the given Observable.
     */
    void drawImage(Observable<Line> lineObs) {
        mandelPanel.clear();
        lineObs.observeOn(SwingScheduler.getInstance())
                .doOnError(this::showError)
                .retry()
                .subscribe(
                        mandelPanel::draw,
                        throwable -> System.err.println("Error: " + throwable),
                        mandelPanel::finish);
    }

    private void showError(Throwable throwable) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Failed to calculate image:\n" + throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void initComponents() {
        mandelPanel = new MandelPanel();

        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mandel");
        setLayout(new BorderLayout());

        add(mandelPanel, BorderLayout.CENTER);

        pack();
    }

    private JMenuBar createMenuBar() {
        // Menu bar
        JMenu fileMenu = new JMenu();
        fileMenu.setText("File");
        fileMenu.setMnemonic('F');

        newMenuItem = new JMenuItem();
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        fileMenu.add(newMenuItem);

        exitMenuItem = new JMenuItem();
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        JMenu editMenu = new JMenu();
        editMenu.setText("Edit");
        editMenu.setMnemonic('E');

        undoMenuItem = new JMenuItem();
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        undoMenuItem.setText("Undo");
        editMenu.add(undoMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }
}
