package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opentracing.mock.MockSpan;

/**
 * @author Pavol Loffay
 */
public class TracedRunnableTest extends AbstractConcurrentTest {

  protected Runnable toTraced(Runnable runnable) {
    return new TracedRunnable(runnable, mockTracer);
  }

  @Test
  public void testTracedRunnable() throws InterruptedException {
    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.scopeManager().activate(parentSpan);

    Thread thread = createThread(toTraced(new TestRunnable()));
    thread.start();
    thread.join();

    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testTracedRunnableNoParent() throws InterruptedException {
    Thread thread = createThread(toTraced(new TestRunnable()));
    thread.start();
    thread.join();

    assertParentSpan(null);
    assertEquals(1, mockTracer.finishedSpans().size());
  }
}
