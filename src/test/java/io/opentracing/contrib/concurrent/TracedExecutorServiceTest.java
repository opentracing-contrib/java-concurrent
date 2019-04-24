package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import io.opentracing.Scope;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import io.opentracing.mock.MockSpan;

/**
 * @author Pavol Loffay
 */
public class TracedExecutorServiceTest extends AbstractConcurrentTest {
  private static final int NUMBER_OF_THREADS = 4;

  protected ExecutorService toTraced(ExecutorService executorService) {
    return new TracedExecutorService(executorService, mockTracer);
  }

  protected ExecutorService toTracedCreatingParent(ExecutorService executorService) {
    return new TracedExecutorService(executorService, mockTracer, false);
  }

  @Test
  public void testExecuteRunnable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.execute(new TestRunnable());
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testExecuteRunnableNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.execute(new TestRunnable());
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.submit(new TestRunnable());
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnableNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.submit(new TestRunnable());
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnableTyped() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.submit(new TestRunnable(), new Object());
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitRunnableTypedNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.submit(new TestRunnable(), new Object());
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitCallable() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.submit(new TestCallable());
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testSubmitCallableNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.submit(new TestCallable());
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAll() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()));
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAllNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()));
    countDownLatch.await();
    assertEquals(3, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAllTimeUnit() throws InterruptedException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()), 1, TimeUnit.SECONDS);
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAllTimeUnitNoParent() throws InterruptedException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    countDownLatch = new CountDownLatch(2);
    executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()), 1, TimeUnit.SECONDS);
    countDownLatch.await();
    assertEquals(3, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAnyTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.invokeAny(Arrays.asList(new TestCallable()), 1, TimeUnit.SECONDS);
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAnyTimeUnitNoParent() throws InterruptedException, ExecutionException, TimeoutException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.invokeAny(Arrays.asList(new TestCallable()), 1, TimeUnit.SECONDS);
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAny() throws InterruptedException, ExecutionException {
    ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

    MockSpan parentSpan = mockTracer.buildSpan("foo").start();
    Scope scope = mockTracer.scopeManager().activate(parentSpan);
    executorService.invokeAny(Arrays.asList(new TestCallable()));
    scope.close();

    countDownLatch.await();
    assertParentSpan(parentSpan);
    assertEquals(1, mockTracer.finishedSpans().size());
  }

  @Test
  public void testInvokeAnyNoParent() throws InterruptedException, ExecutionException {
    ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    executorService.invokeAny(Arrays.asList(new TestCallable()));
    countDownLatch.await();
    assertEquals(2, mockTracer.finishedSpans().size());
  }

}
