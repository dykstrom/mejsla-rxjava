package se.dykstrom.rxjava;

import java.util.Collection;
import java.util.HashSet;

import rx.Observable;

public class Primes {

    public static Observable<Integer> primesCollection(Integer to) {
        Collection<Integer> primes = new HashSet<>();
        return Observable.range(2, to)
                .filter(integer -> {
                    boolean isPrime = isPrime(integer, primes);
                    if (isPrime) primes.add(integer);
                    return isPrime;
                });
    }

    private static boolean isPrime(Integer integer, Collection<Integer> primes) {
        return primes.stream().noneMatch(prime -> integer % prime == 0);
    }

    public static Observable<Integer> primesKarl(Integer to) {
        return sieve(Observable.range(2, to), (int) Math.sqrt(to));
    }

    private static Observable<Integer> sieve(Observable<Integer> s, Integer levels) {
        if (levels <= 1) return s;
        final int head = s.toBlocking().first();
        final Observable<Integer> tail = s.skip(1).filter(i -> i % head != 0);
        return sieve(tail, levels - 1).startWith(head);
    }

    public static Observable<Integer> primesTailRecursive(Integer to) {
        return primesIter(Observable.range(2, to), Observable.empty());
    }

    private static Observable<Integer> primesIter(Observable<Integer> integers, Observable<Integer> result) {
        final int head = integers.toBlocking().firstOrDefault(-1);
        if (head == -1) {
            return result;
        }
        final Observable<Integer> tail = integers.skip(1).filter(i -> i % head != 0);
        return primesIter(tail, result.concatWith(Observable.just(head)));
    }
}
