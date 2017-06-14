package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import io.opentracing.mock.MockSpan;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TracedExecutorServiceTest extends AbstractConcurrentTest {
  private static final int NUMBER_OF_THREADS = 4;

  @Test
  public void testExecuteRunnable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.execute(toTraced(new TestRunnable()));

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.submit(toTraced(new TestRunnable()));

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnableTyped() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.submit(toTraced(new TestRunnable()), new Object());

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitCallable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.submit(toTraced(new TestCallable()));

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAll() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(toTraced(new TestCallable()), toTraced(new TestCallable())));

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAllTimeUnit() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(toTraced(new TestCallable()), toTraced(new TestCallable())), 1,
        TimeUnit.SECONDS);

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAnyTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.invokeAny(Arrays.asList(toTraced(new TestCallable())), 1, TimeUnit.SECONDS);

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAny() throws InterruptedException, ExecutionException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
    mockTracer.makeActive(parentSpan);
    executorService.invokeAny(Arrays.asList(toTraced(new TestCallable())));

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

}
