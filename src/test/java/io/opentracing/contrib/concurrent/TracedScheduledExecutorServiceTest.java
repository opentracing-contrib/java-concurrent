package io.opentracing.contrib.concurrent;

import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Jose Montoya
 */
public class TracedScheduledExecutorServiceTest extends AbstractConcurrentTest {
	private static final int NUMBER_OF_THREADS = 4;

	protected ScheduledExecutorService toTraced(ScheduledExecutorService scheduledExecutorService) {
		return new TracedScheduledExecutorService(scheduledExecutorService, mockTracer);
	}

	@Test
	public void scheduleRunnableTest() throws InterruptedException {
		ScheduledExecutorService executorService = toTraced(Executors.newScheduledThreadPool(NUMBER_OF_THREADS));

		MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
		mockTracer.scopeManager().activate(parentSpan, true);
		executorService.schedule(new TestRunnable(), 300, TimeUnit.MILLISECONDS);

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, mockTracer.finishedSpans().size());
	}

	@Test
	public void scheduleCallableTest() throws InterruptedException {
		ScheduledExecutorService executorService = toTraced(Executors.newScheduledThreadPool(NUMBER_OF_THREADS));

		MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
		mockTracer.scopeManager().activate(parentSpan, true);
		executorService.schedule(new TestCallable(), 300, TimeUnit.MILLISECONDS);

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, mockTracer.finishedSpans().size());
	}

	@Test
	public void scheduleAtFixedRateTest() throws InterruptedException {
		countDownLatch = new CountDownLatch(2);
		ScheduledExecutorService executorService = toTraced(Executors.newScheduledThreadPool(NUMBER_OF_THREADS));

		MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
		mockTracer.scopeManager().activate(parentSpan, true);
		executorService.scheduleAtFixedRate(new TestRunnable(), 0, 300, TimeUnit.MILLISECONDS);

		countDownLatch.await();
		executorService.shutdown();
		assertParentSpan(parentSpan);
		assertEquals(2, mockTracer.finishedSpans().size());
	}

	@Test
	public void scheduleWithFixedDelayTest() throws InterruptedException {
		countDownLatch = new CountDownLatch(2);
		ScheduledExecutorService executorService = toTraced(Executors.newScheduledThreadPool(NUMBER_OF_THREADS));

		MockSpan parentSpan = mockTracer.buildSpan("foo").startManual();
		mockTracer.scopeManager().activate(parentSpan, true);
		executorService.scheduleWithFixedDelay(new TestRunnable(), 0, 300, TimeUnit.MILLISECONDS);

		countDownLatch.await();
		executorService.shutdown();
		assertParentSpan(parentSpan);
		assertEquals(2, mockTracer.finishedSpans().size());
	}
}
