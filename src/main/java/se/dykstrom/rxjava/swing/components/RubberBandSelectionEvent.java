package se.dykstrom.rxjava.swing.components;

import java.awt.Rectangle;
import java.util.EventObject;

/**
 * An event which indicates that a rubber band selection occurred on a component.
 *
 * @author Johan Dykstrom
 */
public class RubberBandSelectionEvent extends EventObject {

    /** The rectangle that bounds the rubber band selection. */
    private final Rectangle bounds;

    /**
     * Creates a new selection event with the given source and selection bounds.
     */
    RubberBandSelectionEvent(Object source, Rectangle bounds) {
        super(source);
        this.bounds = bounds;
    }

    @Override
    public String toString() {
        return RubberBandSelectionEvent.class.getSimpleName() + "[AREA_SELECTED," + bounds + "] on " + source;
    }

    /**
     * Returns the selection bounds.
     */
    public Rectangle getSelectionBounds() {
        return bounds;
    }
}
