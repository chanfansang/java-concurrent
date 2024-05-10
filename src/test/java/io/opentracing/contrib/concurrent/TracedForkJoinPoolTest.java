package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 */
public class TracedForkJoinPoolTest extends AbstractConcurrentTest {

  protected TracedForkJoinPool toTraced() {
    return TracedForkJoinPool.commonPool(mockTracer);
  }
  protected <V> FutureTask<V> toFutureTask(Callable<V> callable) {
    return new FutureTask<V>(callable);
  }

  @Test
  public void testExecute() throws InterruptedException, ExecutionException {
    TracedForkJoinPool tracedForkJoinPool = toTraced();

    RecursiveTask<Integer> task = new RecursiveTask<Integer>() {
      @Override
      protected Integer compute() {
        System.out.println("Task running in TracedForkJoinPool");
        return 42;
      }
    };
    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);

    tracedForkJoinPool.execute(task);
    task.get();

    scope.close();

    assertParentSpan(parentSpan);
    assertEquals(0, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmit() throws InterruptedException, ExecutionException {
    TracedForkJoinPool tracedForkJoinPool = toTraced();
    RecursiveTask<Integer> task = new RecursiveTask<Integer>() {
      @Override
      protected Integer compute() {
        System.out.println("Task running in TracedForkJoinPool");
        return 42;
      }
    };
    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    ForkJoinTask<?> result = tracedForkJoinPool.submit(task);
    result.get();
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(0, mockTracer.finishedSpans().size());
  }

}
