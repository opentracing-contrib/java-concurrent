package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.AutoFinishScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Jose Montoya
 */
public class TracedAutoFinishTest {
  private static final int NUMBER_OF_THREADS = 4;
  protected MockTracer mockTracer = new MockTracer(new AutoFinishScopeManager());

  @Before
  public void before() {
    mockTracer.reset();
  }

  @Test
  public void autoFinishScopeExecuteTest() throws InterruptedException {
    Executor executor = new TracedExecutor(Executors.newFixedThreadPool(10), mockTracer);

    try (Scope scope = mockTracer.buildSpan("auto-finish").startActive(true)){
      executor.execute(new TestRunnable());
    }

    assertNull(mockTracer.scopeManager().active());
    assertNull(mockTracer.activeSpan());
    assertEquals(0, mockTracer.finishedSpans().size());
    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(mockTracer), equalTo(1));
    assertEquals(1, mockTracer.finishedSpans().size());
  }


  @Test
  public void autoFinishScopeSubmitCallableTest() throws InterruptedException {
    ExecutorService executorService =
            new TracedExecutorService(Executors.newFixedThreadPool(NUMBER_OF_THREADS), mockTracer);

    try (Scope scope = mockTracer.buildSpan("auto-finish").startActive(true)) {
      executorService.submit(new TestCallable());
    }

    assertNull(mockTracer.scopeManager().active());
    assertNull(mockTracer.activeSpan());
    assertEquals(0, mockTracer.finishedSpans().size());
    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(mockTracer), equalTo(1));
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void autoFinishScopeScheduleAtFixedRateTest() throws InterruptedException {
    final CountDownLatch countDownLatch = new CountDownLatch(2);

    ScheduledExecutorService executorService =
            new TracedScheduledExecutorService(Executors.newScheduledThreadPool(NUMBER_OF_THREADS), mockTracer);

    try (Scope scope = mockTracer.buildSpan("auto-finish").startActive(true)){
      executorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(300);
            countDownLatch.countDown();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, 0, 200, TimeUnit.MILLISECONDS);
    }

    assertNull(mockTracer.scopeManager().active());
    assertNull(mockTracer.activeSpan());
    assertEquals(0, mockTracer.finishedSpans().size());
    countDownLatch.await();
    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(mockTracer), equalTo(1));
    executorService.shutdown();
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  public static Callable<Integer> finishedSpansSize(final MockTracer tracer) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return tracer.finishedSpans().size();
      }
    };
  }

  class TestRunnable implements Runnable {
    @Override
    public void run() {
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  class TestCallable implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}