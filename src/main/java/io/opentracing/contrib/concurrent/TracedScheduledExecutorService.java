package io.opentracing.contrib.concurrent;

import io.opentracing.Tracer;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jose Montoya
 */
public class TracedScheduledExecutorService extends TracedExecutorService implements ScheduledExecutorService {

	private final ScheduledExecutorService delegate;

	public TracedScheduledExecutorService(ScheduledExecutorService delegate, Tracer tracer) {
		super(delegate, tracer);
		this.delegate = delegate;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
		return delegate.schedule(tracer.activeSpan() == null ? runnable :
				new TracedRunnable(runnable, tracer), delay, timeUnit);
	}

	@Override
	public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
		return delegate.schedule(tracer.activeSpan() == null ? callable :
				new TracedCallable<T>(callable, tracer), delay, timeUnit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
		return delegate.scheduleAtFixedRate(tracer.activeSpan() == null ? runnable :
				new TracedRunnable(runnable, tracer), initialDelay, period, timeUnit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
		return delegate.scheduleWithFixedDelay(tracer.activeSpan() == null ? runnable :
				new TracedRunnable(runnable, tracer), initialDelay, delay, timeUnit);
	}
}