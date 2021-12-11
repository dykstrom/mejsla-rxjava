package se.dykstrom.rxjava.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class enables the user to select a rectangular area of the given component using the mouse.
 * A rubber band rectangle is drawn on top of the component to visualize the selected area. When
 * the user releases the mouse, a {@code RubberBandSelectionEvent} is fired to all registered
 * {@code RubberBandSelectionListener}s.
 *
 * @author Johan Dykstrom
 * @see RubberBandSelectionEvent
 * @see RubberBandSelectionListener
 */
@SuppressWarnings("unused")
public class RubberBandSelector {

    /** The component on which to draw and select. */
    private final Component component;

    /** The list of registered event listeners. */
    private final List<RubberBandSelectionListener> listeners = new ArrayList<>();

    public RubberBandSelector(Component component) {
        this.component = component;

        // Add mouse event listeners to the component, so we can track mouse movement
        MouseRecorder recorder = new MouseRecorder();
        component.addMouseListener(recorder);
        component.addMouseMotionListener(recorder);
    }

    /**
     * Adds the specified listener to receive events from this component.
     */
    public void addRubberBandListener(RubberBandSelectionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the given listener, so it will no longer receive any events from this component.
     */
    public void removeRubberBandListener(RubberBandSelectionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Fires a {@link RubberBandSelectionEvent} to all registered listeners.
     */
    private void fireRubberBandSelectionEvent(Rectangle rectangle) {
        synchronized (listeners) {
            for (RubberBandSelectionListener listener : listeners) {
                listener.valueChanged(new RubberBandSelectionEvent(component, rectangle));
            }
        }
    }

    /**
     * Private helper class that records mouse movement, and draws the rubber band rectangle.
     *
     * @author Johan Dykstrom
     */
    private class MouseRecorder extends MouseAdapter {

        private int startX,  startY,  lastX,  lastY;

        /**
         * Records the location of the top-left corner of rectangle, and starts
         * to draw the rubber band rectangle.
         */
        @Override
        public void mousePressed(MouseEvent event) {
            startX = event.getX();
            startY = event.getY();
            lastX = startX;
            lastY = startY;

            Graphics2D g = (Graphics2D) component.getGraphics();
            g.setXORMode(Color.LIGHT_GRAY);
            g.draw(toRectangle(startX, startY, lastX, lastY));
        }

        /**
         * Erases the last rubber band rectangle when the user releases the
         * mouse, and fires a {@link RubberBandSelectionEvent} to the event
         * listeners.
         */
        @Override
        public void mouseReleased(MouseEvent event) {
            Rectangle rectangle = toRectangle(startX, startY, lastX, lastY);

            Graphics2D g = (Graphics2D) component.getGraphics();
            g.setXORMode(Color.LIGHT_GRAY);
            g.draw(rectangle);

            if ((startX != lastX) && (startY != lastY)) {
                fireRubberBandSelectionEvent(rectangle);
            }
        }

        /**
         * Draws a rubber band rectangle, from the location where the mouse was
         * first clicked to the location where the mouse has been dragged.
         */
        @Override
        public void mouseDragged(MouseEvent event) {
            int x = event.getX();
            int y = event.getY();

            // Keep the rectangle within the component's borders
            if (x < 0) {
                x = 0;
            }
            if (x > component.getWidth() - 1) {
                x = component.getWidth() - 1;
            }
            if (y < 0) {
                y = 0;
            }
            if (y > component.getHeight() - 1) {
                y = component.getHeight() - 1;
            }

            Graphics2D g = (Graphics2D) component.getGraphics();
            g.setXORMode(Color.LIGHT_GRAY);
            g.draw(toRectangle(startX, startY, lastX, lastY));
            g.draw(toRectangle(startX, startY, x, y));

            lastX = x;
            lastY = y;
        }

        /**
         * Returns the selected rectangle, adjusting the x, y, w, h to
         * correctly accommodate for the opposite corner of the rubber band box
         * relative to the start position.
         */
        private Rectangle toRectangle(int startX, int startY, int stopX, int stopY) {
            int x, y, w, h;
            x = Math.min(startX, stopX);
            y = Math.min(startY, stopY);
            w = Math.abs(startX - stopX);
            h = Math.abs(startY - stopY);
            return new Rectangle(x, y, w, h);
        }
    }
}
