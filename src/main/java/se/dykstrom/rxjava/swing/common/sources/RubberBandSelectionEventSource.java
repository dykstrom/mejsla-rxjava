package se.dykstrom.rxjava.swing.common.sources;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;
import se.dykstrom.rxjava.swing.common.SwingObservables;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionEvent;
import se.dykstrom.rxjava.swing.components.RubberBandSelectionListener;
import se.dykstrom.rxjava.swing.components.RubberBandSelector;

import javax.swing.*;

public enum RubberBandSelectionEventSource { ; // no instances

    /**
     * @see SwingObservables#fromRubberBandSelectionEvents(JComponent)
     */
    public static Observable<RubberBandSelectionEvent> fromRubberBandSelectionEventsOf(JComponent component) {
        return Observable.create(new Observable.OnSubscribe<RubberBandSelectionEvent>() {
            @Override
            public void call(Subscriber<? super RubberBandSelectionEvent> subscriber) {
                RubberBandSelectionListener listener = subscriber::onNext;
                RubberBandSelector rubberBandSelector = new RubberBandSelector(component);
                rubberBandSelector.addRubberBandListener(listener);
                subscriber.add(Subscriptions.create(() -> rubberBandSelector.removeRubberBandListener(listener)));
            }
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }
}
