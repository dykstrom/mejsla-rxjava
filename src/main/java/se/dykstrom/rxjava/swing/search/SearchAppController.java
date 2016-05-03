package se.dykstrom.rxjava.swing.search;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import se.dykstrom.rxjava.common.Observables;
import se.dykstrom.rxjava.common.operators.ToUrl;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static se.dykstrom.rxjava.swing.search.SearchAppUtils.titleObs;

public class SearchAppController {

    public static final int TYPE_DELAY = 500;
    public static final List<String> NO_RESULTS = Collections.<String>emptyList();
    public static final String NO_DOCUMENT = "<html><body></body></html>";

    private static final Scheduler COMPUTATION_SCHEDULER = Schedulers.computation();
    private static final Scheduler SWING_SCHEDULER = SwingScheduler.getInstance();

    private final SearchAppView view;

    public SearchAppController(SearchAppView view) {
        this.view = view;
        initSubscriptions();
    }

    private void initSubscriptions() {
        titlesFromSearchTextObs(view.typedSearchTextObs())
                .observeOn(SWING_SCHEDULER)
                .doOnError(this::showSearchError)
                .retry()
                .subscribe(view::setResults);
        Observable.merge(documentFromTitleObs(view.selectedTitleObs()), documentFromLinkObs(view.selectedLinkObs()))
                .observeOn(SWING_SCHEDULER)
                .doOnError(this::showLoadError)
                .retry()
                .subscribe(view::setDocument);
    }

    /**
     * Returns an Observable that takes the search texts that are emitted from the source Observable,
     * searches Wikipedia, and emits the titles of the articles containing those texts. If the source
     * Observable emits an empty search text, this Observable emits an empty document. Note that there
     * is a delay, so if the user types fast, only a single search is performed.
     *
     * @param searchTextObs The source Observable, emitting search texts entered by the user.
     * @return An Observable that results from searching Wikipedia.
     */
    public static Observable<List<String>> titlesFromSearchTextObs(Observable<String> searchTextObs) {
        return titlesFromSearchTextObs(searchTextObs, Observables::fromUrl);
    }

    /**
     * Like {@link #titlesFromSearchTextObs(Observable)} but uses {@code observableFactory} to create the
     * Observable that loads a document from a URL.
     *
     * @param searchTextObs The source Observable, emitting search texts entered by the user.
     * @param observableFactory A factory used to create an Observable that can load a document from an URL.
     * @return An Observable that results from searching Wikipedia.
     */
    public static Observable<List<String>> titlesFromSearchTextObs(Observable<String> searchTextObs, Func1<URL, Observable<String>> observableFactory) {
        return searchTextObs
                .observeOn(COMPUTATION_SCHEDULER)
                .debounce(TYPE_DELAY, TimeUnit.MILLISECONDS)
                .groupBy(String::isEmpty)
                .flatMap(observable -> observable.getKey() ?
                        observable
                                .map(ignore -> NO_RESULTS) :
                        observable
                                .map(SearchAppUtils::toSearchQuery)
                                .lift(new ToUrl())
                                .switchMap(url -> observableFactory.call(url)
                                        .observeOn(COMPUTATION_SCHEDULER)
                                        .flatMap(document -> titleObs(document).toSortedList())));
    }

    /**
     * Returns an Observable that takes the selected titles that are emitted from the source Observable,
     * and loads the corresponding document from Wikipedia. If the source Observable emits an empty title,
     * this Observable emits an empty document.
     *
     * @param titleObs The source Observable, emitting article titles selected by the user.
     * @return An Observable that emits documents (articles) from Wikipedia.
     */
    public static Observable<String> documentFromTitleObs(Observable<String> titleObs) {
        return documentFromTitleObs(titleObs, Observables::fromUrl);
    }

    /**
     * Like {@link #documentFromTitleObs(Observable)} but uses {@code observableFactory} to create the
     * Observable that loads a document from a URL.
     *
     * @param titleObs The source Observable, emitting article titles selected by the user.
     * @param observableFactory A factory used to create an Observable that can load a document from an URL.
     * @return An Observable that emits documents (articles) from Wikipedia.
     */
    public static Observable<String> documentFromTitleObs(Observable<String> titleObs, Func1<URL, Observable<String>> observableFactory) {
        return titleObs
                .observeOn(COMPUTATION_SCHEDULER)
                .groupBy(String::isEmpty)
                .flatMap(observable -> observable.getKey() ?
                        observable
                                .map(ignore -> NO_DOCUMENT) :
                        observable
                                .map(SearchAppUtils::toArticleQuery)
                                .lift(new ToUrl())
                                .switchMap(url -> observableFactory.call(url)
                                        .map(SearchAppUtils::fixWikipediaLinks)));
    }

    /**
     * Returns an Observable that takes the selected link URLs that are emitted from the source Observable,
     * and loads the corresponding document.
     *
     * @param linkObs The source Observable, emitting link URLs selected by the user.
     * @return An Observable that emits documents loaded from the URLs.
     */
    public static Observable<String> documentFromLinkObs(Observable<URL> linkObs) {
        return documentFromLinkObs(linkObs, Observables::fromUrl);
    }

    /**
     * Like {@link #documentFromLinkObs(Observable)} but uses {@code observableFactory} to create the
     * Observable that loads a document from a URL.
     *
     * @param linkObs The source Observable, emitting link URLs selected by the user.
     * @param observableFactory A factory used to create an Observable that can load a document from an URL.
     * @return An Observable that emits documents loaded from the URLs.
     */
    public static Observable<String> documentFromLinkObs(Observable<URL> linkObs, Func1<URL, Observable<String>> observableFactory) {
        return linkObs
                .switchMap(url -> observableFactory.call(url)
                        .map(SearchAppUtils::fixWikipediaLinks));
    }

    private void showSearchError(Throwable throwable) {
        view.showError("Search failed", throwable);
    }

    private void showLoadError(Throwable throwable) {
        view.showError("Failed to load page", throwable);
        throwable.printStackTrace();
    }
}
