package io.opentracing.contrib.concurrent;

import io.opentracing.Tracer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author chanfan
 * @date 2024-05-09 17:40
 */
public class TracedCompletableFuture<T> extends CompletableFuture<T> {
    private final boolean traceWithActiveSpanOnly;
    protected static Tracer tracer;


    public TracedCompletableFuture(Tracer tracer) {
        super();
        this.traceWithActiveSpanOnly = true;
        this.tracer = tracer;
    }

    public static TracedCompletableFuture buildTrace(Tracer tracer){
        return new TracedCompletableFuture(tracer);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncPool);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,
                                                       Executor executor) {
        TracedExecutor tracedExecutor = new TracedExecutor(executor, tracer);
        return CompletableFuture.supplyAsync(supplier, tracedExecutor);
    }

    public static <T> CompletableFuture<T> thenApplyAsync(CompletableFuture<T> future, Function<T, T> function) {

        return future.thenApplyAsync(function, asyncPool);
    }
    public static <T> CompletableFuture<T> thenApplyAsync(CompletableFuture<T> future, Function<T, T> function, Executor executor) {

        TracedExecutor tracedExecutor = new TracedExecutor(executor, tracer);
        return future.thenApplyAsync(function, tracedExecutor);
    }


    public static CompletableFuture<Void> runAsync(Runnable runnable) {

        return CompletableFuture.runAsync(runnable, asyncPool);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable,
                                                   Executor executor) {

        TracedExecutor tracedExecutor = new TracedExecutor(executor, tracer);
        return CompletableFuture.runAsync(runnable, tracedExecutor);
    }


    private static final boolean useCommonPool =
            (TracedForkJoinPool.getCommonPoolParallelism() > 1);

    private static final Executor asyncPool = useCommonPool ?
            TracedForkJoinPool.commonPool(tracer) : new ThreadPerTaskExecutor();

    final static class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            Runnable runnableTask = new TracedRunnable(r, tracer);
            new Thread(runnableTask).start();
        }
    }

}