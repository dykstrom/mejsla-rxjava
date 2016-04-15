package se.dykstrom.rxjava;

import rx.Observable;
import rx.observables.GroupedObservable;
import se.dykstrom.rxjava.common.utils.Utils;

import java.util.List;
import java.util.Objects;

public class Words {

    /**
     * Returns an Observable that splits the strings emitted by the source observable into words.
     */
    public static Observable<String> toWords(Observable<String> strings) {
        return strings.flatMap(string -> Observable.from(string.split("[ ]+")));
    }

    /**
     * Returns an Observable that cleans the stream of words emitted by the source observable
     * by removing non-alphabetical characters and strings that are not real words.
     */
    public static Observable<String> clean(Observable<String> words) {
        return words.map(String::toLowerCase).filter(word -> word.matches(".*[a-z]+.*")).map(Utils::trimNonAlpha);
    }

    /**
     * Returns an Observable that groups all equal words together and emits a stream of GroupedObservables.
     */
    public static Observable<GroupedObservable<String, String>> groupByWord(Observable<String> words) {
        return words.groupBy(word -> word);
    }

    /**
     * Returns an Observable that counts the number of words in each group, and emits a stream
     * of {@link WordCount}s.
     */
    public static Observable<WordCount> count(Observable<GroupedObservable<String, String>> groups) {
        return groups.flatMap(group -> Observable.zip(Observable.just(group.getKey()), group.count(), WordCount::new));
    }

    /**
     * Returns an Observable that emits a list that contains the items emitted by the source Observable,
     * in a sorted order based on their word count.
     */
    public static Observable<List<WordCount>> sort(Observable<WordCount> wordCounts) {
        return wordCounts.toSortedList((wc1, wc2) -> wc2.getValue().compareTo(wc1.getValue()));
    }

    /**
     * Returns an Observable that emits a list of words, ordered after how often they appear in
     * the strings emitted by the source Observable.
     */
    public static Observable<List<WordCount>> wordsByCount(Observable<String> strings) {
        return sort(count(groupByWord(clean(toWords(strings)))));
    }

    /**
     * A helper class that holds a word and its count.
     */
    static class WordCount {

        private final String key;
        private final Integer value;

        public WordCount(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + key + "->" + value + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WordCount that = (WordCount) obj;
            return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        public String getKey() {
            return key;
        }

        public Integer getValue() {
            return value;
        }
    }
}
