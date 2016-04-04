package se.dykstrom.rxjava.swing.common;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import se.dykstrom.rxjava.swing.common.operators.OnSubscribeFromUrl;
import se.dykstrom.rxjava.swing.common.sources.HyperlinkEventSource;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;

public final class Observables {

    private Observables() {
        // Hidden
    }

    /**
     * Returns an Observable that reads a document from the given URL, emits it as a single string,
     * and then completes.
     *
     * Scheduler: this version of {@code fromUrl} operates by default on the {@code IO} {@link Scheduler}.
     */
    public static Observable<String> fromUrl(URL url) {
        return fromUrl(url, Schedulers.io());
    }

    /**
     * Returns an Observable that reads a document from the given URL, emits it as a single string,
     * and then completes.
     *
     * Scheduler: you specify which {@link Scheduler} this operator will use.
     */
    public static Observable<String> fromUrl(URL url, Scheduler scheduler) {
        return Observable.create(new OnSubscribeFromUrl(url, scheduler));
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
