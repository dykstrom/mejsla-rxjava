package se.dykstrom.rxjava.swing.mandel;

import rx.Observable;

final class LineObservable {

    private LineObservable() { }

    /**
     * Returns an Observable that emits a number of {@link Line} objects that together make up
     * one segment of the image. The lines are calculated by a {@link LineProducer}.
     *
     * @param parameters Parameters for calculating one segment of the image.
     * @return An Observable that emits image lines.
     */
    static Observable<Line> fromParameters(Parameters parameters) {
        return Observable.create(subscriber -> subscriber.setProducer(new LineProducer(parameters, subscriber)));
    }
}
