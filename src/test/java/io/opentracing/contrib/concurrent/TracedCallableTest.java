package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Test;

import io.opentracing.mock.MockSpan;

/**
 * @author Pavol Loffay
 */
public class TracedCallableTest extends AbstractConcurrentTest {

  protected <V> Callable<V> toTraced(Callable<V> callable) {
    return new TracedCallable<V>(callable, mockTracer.activeSpan());
  }

  @Test
  public void testTracedCallable() throws InterruptedException, ExecutionException {
    MockSpan parent = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parent);

    FutureTask<Void> futureTask = new FutureTask<Void>(toTraced(new TestCallable()));
    Thread thread = createThread(futureTask);
    thread.start();
    futureTask.get();
    thread.join();

    assertParentSpan(parent);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testTracedCallableNoParent() throws Throwable {
    FutureTask<Void> futureTask = new FutureTask<Void>(toTraced(new TestCallable()));
    Thread thread = createThread(futureTask);
    thread.start();
    futureTask.get();
    thread.join();

    assertParentSpan(null);
    assertEquals(1, mockTracer.finishedSpans().size());
  }
}
