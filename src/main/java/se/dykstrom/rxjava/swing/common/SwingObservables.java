package se.dykstrom.rxjava.swing.common;

import rx.Observable;
import se.dykstrom.rxjava.swing.common.sources.HyperlinkEventSource;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public final class SwingObservables {

    private SwingObservables() {
        // Hidden
    }

    /**
     * Creates an Observable corresponding to hyperlink events from a {@code JEditorPane}.
     *
     * @param editorPane The {@code JEditorPane} to register the observable for.
     * @return Observable emitting the hyperlink events.
     */
    public static Observable<HyperlinkEvent> fromHyperlinkEvents(JEditorPane editorPane) {
        return HyperlinkEventSource.fromHyperlinkEventsOf(editorPane);
    }
}
