package se.dykstrom.rxjava.swing.common;

import rx.Observable;
import se.dykstrom.rxjava.swing.common.sources.HyperlinkEventSource;
import se.dykstrom.rxjava.swing.common.sources.RubberBandSelectionEventSource;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public final class SwingObservables {

    private SwingObservables() { }

    /**
     * Creates an Observable corresponding to hyperlink events from a {@code JEditorPane}.
     *
     * @param editorPane The {@code JEditorPane} to register the observable for.
     * @return Observable emitting the hyperlink events.
     */
    public static Observable<HyperlinkEvent> fromHyperlinkEvents(JEditorPane editorPane) {
        return HyperlinkEventSource.fromHyperlinkEventsOf(editorPane);
    }

    /**
     * Creates an Observable corresponding to rubber band selection events from a {@code JComponent}.
     *
     * @param component The {@code JComponent} to register the observable for.
     * @return Observable emitting the rubber band selection events.
     */
    public static Observable<RubberBandSelectionEvent> fromRubberBandSelectionEvents(JComponent component) {
        return RubberBandSelectionEventSource.fromRubberBandSelectionEventsOf(component);
    }
}
