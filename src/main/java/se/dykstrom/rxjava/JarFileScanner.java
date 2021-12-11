package se.dykstrom.rxjava;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.dykstrom.rxjava.common.DirectoryStreamObservable;
import se.dykstrom.rxjava.common.ZipObservable;

/**
 * A program that scans a directory recursively for jar files, and for each jar file found,
 * counts the class files within it.
 */
public class JarFileScanner {

    public static void main(String[] args) throws Exception {
        String directoryName = args.length == 1 ? args[0] : "target";
        Path directoryPath = Paths.get(directoryName);

        Observable<Path> jarFiles = listJarFilesObs(directoryPath);
        Observable<Integer> classCount = classCountObs(jarFiles);

        CountDownLatch latch = new CountDownLatch(1);
        classCount.finallyDo(latch::countDown).subscribe(
                count -> {
                    System.out.println("---------------------------");
                    log(count, "total");
                },
                throwable -> System.err.println("Error: " + throwable)
        );
        latch.await();
    }

    /**
     * Returns an Observable that emits a single item: the total number of class files in
     * all the jar files emitted by the source Observable.
     */
    public static Observable<Integer> classCountObs(Observable<Path> jarFiles) {
        return jarFiles.flatMap(jarFile -> classFileObs(jarFile)
                .onBackpressureBuffer(100000)
                .observeOn(Schedulers.io())
                .count()
                .doOnNext(count -> log(count, jarFile.toString())))
                .reduce(Integer::sum);
    }

    /**
     * Returns an Observable that emits the names of all class files in the given JAR file.
     */
    public static Observable<String> classFileObs(Path jarFile) {
        return ZipObservable.fromPath(jarFile)
                .doOnError(throwable -> System.err.println("Failed to read " + jarFile + ": " + throwable))
                .onErrorResumeNext(Observable.empty())
                .filter(zipEntry -> !zipEntry.isDirectory())
                .map(ZipEntry::getName)
                .filter(name -> name.endsWith(".class"));
    }

    /**
     * Returns an Observable that emits all JAR files in the given directory and its subdirectories,
     * in the form of {@link Path} objects.
     */
    private static Observable<Path> listJarFilesObs(Path directory) {
        return DirectoryStreamObservable.fromPathRecursive(directory)
                .filter(path -> path.toFile().getName().endsWith(".jar"));
    }

    private static void log(int count, String name) {
        String[] parts = name.split("\\\\");
        System.out.printf("%6d class files in %-100s     %s\n", count, parts[parts.length - 1], Thread.currentThread().getName());
    }
}
