package se.dykstrom.rxjava.common.functions;

import rx.functions.Function;

/**
 * A functional interface copied from RxJavaAsyncUtils.
 */
@FunctionalInterface
public interface ThrowingFunc2<T1, T2, R> extends Function {
    R call(T1 t1, T2 t2) throws Exception;
}
