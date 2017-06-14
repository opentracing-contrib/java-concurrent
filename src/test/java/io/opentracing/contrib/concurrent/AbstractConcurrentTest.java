package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.junit.Before;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractConcurrentTest {

  class TestRunnable implements Runnable {
    @Override
    public void run() {
      try {
        mockTracer.buildSpan("childRunnable")
            .startActive()
            .close();
      } finally {
        countDownLatch.countDown();
      }
    }
  }

  class TestCallable implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      try {
        mockTracer.buildSpan("childCallable")
            .startActive()
            .close();
      } finally {
        countDownLatch.countDown();
      }
      return null;
    }
  }

  protected CountDownLatch countDownLatch = new CountDownLatch(0);
  protected MockTracer mockTracer = new MockTracer(new ThreadLocalActiveSpanSource());

  @Before
  public void before() {
    countDownLatch = new CountDownLatch(1);
    mockTracer.reset();
  }

  protected void assertParentSpan(MockSpan parent) {
    for (MockSpan child: mockTracer.finishedSpans()) {
      if (child == parent) {
        continue;
      }

      if (parent == null) {
        assertEquals(0, child.parentId());
      } else {
        assertEquals(parent.context().traceId(), child.context().traceId());
        assertEquals(parent.context().spanId(), child.parentId());
      }
    }
  }

  protected Runnable toTraced(Runnable runnable) {
    return new TracedRunnable(runnable, mockTracer.activeSpan());
  }

  public <V> Callable<V> toTraced(Callable<V> callable) {
    return new TracedCallable<V>(callable, mockTracer.activeSpan());
  }

  public ExecutorService toTraced(ExecutorService executorService) {
    return new TracedExecutorService(executorService, mockTracer);
  }

  protected Thread createThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    return thread;
  }

  protected <V> Thread createThread(FutureTask<V> futureTask) {
    Thread thread = new Thread(futureTask);
    return thread;
  }
}
