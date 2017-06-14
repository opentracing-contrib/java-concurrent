package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import io.opentracing.mock.MockSpan;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TracedRunnableTest extends AbstractConcurrentTest {

  @Test
  public void testTracedRunnable() throws InterruptedException {
    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);

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
