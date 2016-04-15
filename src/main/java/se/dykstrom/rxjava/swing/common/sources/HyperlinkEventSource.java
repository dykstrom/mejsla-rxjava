package se.dykstrom.rxjava.swing.common.sources;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;
import se.dykstrom.rxjava.swing.common.SwingObservables;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public enum HyperlinkEventSource { ; // no instances

    /**
     * @see SwingObservables#fromHyperlinkEvents(JEditorPane)
     */
    public static Observable<HyperlinkEvent> fromHyperlinkEventsOf(JEditorPane editorPane) {
        return Observable.create(new Observable.OnSubscribe<HyperlinkEvent>() {
            @Override
            public void call(Subscriber<? super HyperlinkEvent> subscriber) {
                HyperlinkListener listener = subscriber::onNext;
                editorPane.addHyperlinkListener(listener);
                subscriber.add(Subscriptions.create(() -> editorPane.removeHyperlinkListener(listener)));
            }
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }
}
