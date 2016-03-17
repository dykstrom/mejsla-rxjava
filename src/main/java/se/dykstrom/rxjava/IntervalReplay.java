package se.dykstrom.rxjava;

import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static se.dykstrom.rxjava.Utils.subscribePrint;

/**
 * Tests replay of an interval observable by simulating a stream of memory measurements that can be used
 * to draw a memory usage chart. The third subscriber will receive all emitted memory measurements and
 * can draw a complete chart, just like the first and second subscriber.
 */
public class IntervalReplay {

    public static void run() throws Exception {
        Observable<Long> interval = Observable.interval(100L, TimeUnit.MILLISECONDS);
        Observable<String> mapped = interval.map(l -> "Time=" + LocalTime.now() + ", Free memory=" + getFreeMemory() + " Mb");
        ConnectableObservable<String> published = mapped.replay();

        Subscription sub1 = subscribePrint(published, "First");
        Subscription sub2 = subscribePrint(published, "Second");

        published.connect();

        Thread.sleep(300L);
        allocateSomeMemory();

        Subscription sub3 = subscribePrint(published, "Third");

        Thread.sleep(300L);
        garbageCollect();

        Thread.sleep(300L);

        sub1.unsubscribe();
        sub2.unsubscribe();
        sub3.unsubscribe();
    }

    private static void garbageCollect() {
        System.gc();
        log("Main", "Garbage collected memory");
    }

    private static void allocateSomeMemory() {
        double[] array = new double[10000000];
        Arrays.fill(array, 0.0);
        log("Main", "Allocated array of length " + array.length);
    }

    private static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory() / (1024 * 1024);
    }

    private static void log(String name, String msg) {
        System.out.println(Thread.currentThread().getName() + "|" + name + " : " + msg);
    }

    public static void main(String[] args) throws Exception {
        IntervalReplay.run();
    }
}
