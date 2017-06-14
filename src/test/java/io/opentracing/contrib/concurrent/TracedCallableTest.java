package io.opentracing.contrib.concurrent;

import static org.junit.Assert.assertEquals;

import io.opentracing.mock.MockSpan;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TracedCallableTest extends AbstractConcurrentTest {

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
