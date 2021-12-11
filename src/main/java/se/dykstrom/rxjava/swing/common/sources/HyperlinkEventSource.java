package se.dykstrom.rxjava.swing.common.sources;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import rx.Observable;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;
import se.dykstrom.rxjava.swing.common.SwingObservables;

public enum HyperlinkEventSource { ; // no instances

    /**
     * @see SwingObservables#fromHyperlinkEvents(JEditorPane)
     */
    public static Observable<HyperlinkEvent> fromHyperlinkEventsOf(JEditorPane editorPane) {
        return Observable.create((Observable.OnSubscribe<HyperlinkEvent>) subscriber -> {
            HyperlinkListener listener = subscriber::onNext;
            editorPane.addHyperlinkListener(listener);
            subscriber.add(Subscriptions.create(() -> editorPane.removeHyperlinkListener(listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }
}
