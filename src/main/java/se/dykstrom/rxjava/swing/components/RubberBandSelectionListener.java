package se.dykstrom.rxjava.swing.components;

import java.util.EventListener;

/**
 * Interface to be implemented by classes interested in receiving {@link RubberBandSelectionEvent}s.
 *
 * @author Johan Dykstrom
 */
public interface RubberBandSelectionListener extends EventListener {

    /**
     * Called whenever a new selection is made.
     *
     * @param event The event that characterizes the selection.
     */
    void valueChanged(RubberBandSelectionEvent event);
}
