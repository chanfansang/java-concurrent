package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * @author chanfan
 * @date 2024-05-09 18:43
 */
public class TracedForkJoinPool extends ForkJoinPool {
    private final Tracer tracer;
    private final boolean traceWithActiveSpanOnly;
    public TracedForkJoinPool(Tracer tracer) {
        super();
        this.traceWithActiveSpanOnly = true;
        this.tracer = tracer;
    }

    public TracedForkJoinPool(Tracer tracer, boolean traceWithActiveSpanOnly) {
        super();
        this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
        this.tracer = tracer;
    }

    public TracedForkJoinPool(int parallelism, Tracer tracer, boolean traceWithActiveSpanOnly) {
        super(parallelism);
        this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
        this.tracer = tracer;
    }


    @Override
    public void execute(ForkJoinTask<?> task) {
        Span span = createSpan("execute");
        Span toActivate = span != null ? span : tracer.activeSpan();
        try (Scope scope = tracer.activateSpan(toActivate)) {
            super.execute(task);
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }

    @Override
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        Span span = createSpan("submit");
        Span toActivate = span != null ? span : tracer.activeSpan();
        try (Scope scope = tracer.activateSpan(toActivate)) {
            return super.submit(task);
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }

    public static TracedForkJoinPool commonPool(Tracer tracer) {
        return new TracedForkJoinPool(Runtime.getRuntime().availableProcessors(), tracer, true);
    }

    @Override
    public <T> T invoke(ForkJoinTask<T> task) {
        Span span = createSpan("invoke");
        Span toActivate = span != null ? span : tracer.activeSpan();
        try (Scope scope = tracer.activateSpan(toActivate)) {
            return super.invoke(task);
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }

    private Span createSpan(String operationName) {
        if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
            return tracer.buildSpan(operationName).start();
        }
        return null;
    }

}
