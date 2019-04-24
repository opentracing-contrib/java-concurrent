package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import org.junit.Before;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractConcurrentTest {

  class TestRunnable implements Runnable {
    @Override
    public void run() {
      try {
        mockTracer.buildSpan("childRunnable").start().finish();
      } finally {
        countDownLatch.countDown();
      }
    }
  }

  class TestCallable implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      try {
        mockTracer.buildSpan("childCallable").start().finish();
      } finally {
        countDownLatch.countDown();
      }
      return null;
    }
  }

  protected CountDownLatch countDownLatch = new CountDownLatch(0);
  protected MockTracer mockTracer = new MockTracer(new ThreadLocalScopeManager());

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

  protected Thread createThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    return thread;
  }

  protected <V> Thread createThread(FutureTask<V> futureTask) {
    Thread thread = new Thread(futureTask);
    return thread;
  }
}
