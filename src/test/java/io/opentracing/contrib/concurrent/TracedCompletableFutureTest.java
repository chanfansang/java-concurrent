package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 */
public class TracedCompletableFutureTest extends AbstractConcurrentTest {

  protected TracedCompletableFuture toTraced() {
    return TracedCompletableFuture.buildTrace(mockTracer);
  }
  protected <V> FutureTask<V> toFutureTask(Callable<V> callable) {
    return new FutureTask<V>(callable);
  }

  @Test
  public void testRunAsync() {
    TracedCompletableFuture tracedCompletableFuture = toTraced();

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);

    tracedCompletableFuture.runAsync(new TestRunnable());

    scope.close();

    assertParentSpan(parentSpan);
    assertEquals(0, mockTracer.finishedSpans().size());
  }

}
