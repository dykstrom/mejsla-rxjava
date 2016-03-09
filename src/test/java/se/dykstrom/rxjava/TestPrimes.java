package se.dykstrom.rxjava;

import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class TestPrimes {

    private static final int MAX = 2000;

    private static final List<Object> PRIME_LIST = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47);

    private TestSubscriber<Integer> testSubscriber;

    @Before
    public void setUp() {
        testSubscriber = new TestSubscriber<>();
    }

    @Test
    public void testPrimesKarl() {
        doTest("primesKarl", Primes::primesKarl);
    }

    @Test
    public void testPrimesTailRecursive() {
        doTest("primesTailRecursive", Primes::primesTailRecursive);
    }

    @Test
    public void testPrimesCollection() {
        doTest("primesCollection", Primes::primesCollection);
    }

    private void doTest(String name, Function<Integer, Observable<Integer>> primeFunction) {
        long time = Utils.timeRun(() -> {
            Observable<Integer> primes = primeFunction.apply(MAX - 1);
            primes.subscribe(testSubscriber);

            List<Integer> actualPrimes = testSubscriber.getOnNextEvents();

            System.out.printf("[%s] found %d primes: %s\n", name, actualPrimes.size(), actualPrimes);

            assertActualPrimes(actualPrimes);
            testSubscriber.assertCompleted();
        });
        System.out.printf("[%s] finished after %d ms\n", name, time);
    }

    private void assertActualPrimes(List<Integer> actualPrimes) {
        int size = Math.min(PRIME_LIST.size(), actualPrimes.size());
        assertEquals(PRIME_LIST.subList(0, size), actualPrimes.subList(0, size));
    }
}
