package se.dykstrom.rxjava.swing.search;

import java.time.LocalTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import rx.Observable;

public final class SearchAppUtils {

    private static final String WIKIPEDIA_URL = "https://sv.wikipedia.org";

    private static final String SEARCH_URL = WIKIPEDIA_URL + "/w/api.php?action=query&list=search&srsearch={text}&srprop=&srlimit=100&format=json&utf8=";

    private static final String ARTICLE_URL = WIKIPEDIA_URL + "/wiki/{title}";

    private SearchAppUtils() { }

    public static String toSearchQuery(String text) {
        return SEARCH_URL.replace("{text}", text);
    }

    public static String toArticleQuery(String title) {
        return ARTICLE_URL.replace("{title}", title.replace(" ", "_"));
    }

    /**
     * Returns an observable that parses the given JSON string and emits all the page titles that
     * can be extracted from the JSON string.
     */
    public static Observable<String> titleObs(String json) {
        return Observable.create(subscriber -> {
            try {
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonObject query = jsonObject.get("query").getAsJsonObject();
                JsonArray search = query.get("search").getAsJsonArray();
                int size = search.size();
                for (int index = 0; index < size; index++) {
                    String title = search.get(index).getAsJsonObject().get("title").getAsString();
                    if (!subscriber.isUnsubscribed()) subscriber.onNext(title);
                }
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            } catch (JsonSyntaxException e) {
                if (!subscriber.isUnsubscribed()) subscriber.onError(e);
            }
        });
    }

    /**
     * Converts some Wikipedia links to a format that is recognized by the {@code JEditorPane}.
     */
    public static String fixWikipediaLinks(String text) {
        return text.replace("\"//", "\"https://").replace("href=\"/", "href=\"" + WIKIPEDIA_URL + "/");
    }

    public static void log(String msg) {
        System.out.println(LocalTime.now() + " | " + Thread.currentThread().getName() + " | " + msg);
    }

    public static void log(String msg, Throwable throwable) {
        log(msg + ((throwable.getCause() != null) ? ", caused by: " + throwable.getCause() : ""));
    }
}
