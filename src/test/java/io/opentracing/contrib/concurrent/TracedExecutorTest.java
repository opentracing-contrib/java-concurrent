package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;

import io.opentracing.mock.MockSpan;

/**
 * @author Pavol Loffay
 */
public class TracedExecutorTest extends AbstractConcurrentTest {

  @Test
  public void testExecute() throws InterruptedException {
    Executor executor = new TracedExecutor(Executors.newFixedThreadPool(10), mockTracer);

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.scopeManager().activate(parentSpan, true);
    executor.execute(new TestRunnable());

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testExecuteNoParent() throws InterruptedException {
    Executor executor = new TracedExecutor(Executors.newFixedThreadPool(10), mockTracer, false);
    executor.execute(new TestRunnable());
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }
}
