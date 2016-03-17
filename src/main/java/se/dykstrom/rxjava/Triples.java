package se.dykstrom.rxjava;

import rx.Observable;

import java.util.Objects;

import static se.dykstrom.rxjava.Utils.gcd;

public class Triples {

    /**
     * Returns an Observable that emits all Pythagorean triples that can be created from the integers
     * emitted by the source Observable.
     */
    public static Observable<Triple> triples(Observable<Integer> integers) {
        return integers
                .flatMap(a -> integers
                        .flatMap(b -> integers
                                .filter(c -> a * a + b * b == c * c)
                                .map(c -> new Triple(a, b, c))));
    }

    /**
     * Returns an Observable that emits the primitive Pythagorean triples found in the stream of triples
     * emitted by the source Observable.
     */
    public static Observable<Triple> primitive(Observable<Triple> triples) {
        return triples.map(Triple::sorted).distinct().filter(Triple::coprime);
    }

    /**
     * A helper class that represents a Pythagorean triple.
     */
    static class Triple {

        private final int a;
        private final int b;
        private final int c;

        public Triple(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public String toString() {
            return "[" + a + ", " + b + ", " + c + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Triple that = (Triple) obj;
            return this.a == that.a && this.b == that.b && this.c == that.c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c);
        }

        /**
         * Returns a new triple with all fields sorted in ascending order. If the fields are already
         * sorted, the method simply returns this triple.
         */
        public Triple sorted() {
            return (a <= b) ? this : new Triple(b, a, c);
        }

        /**
         * Returns {@code true} if all fields in the triple are co-prime.
         */
        public boolean coprime() {
            return gcd(a, b) == 1 && gcd(a, c) == 1 && gcd(b, c) == 1;
        }
    }
}
