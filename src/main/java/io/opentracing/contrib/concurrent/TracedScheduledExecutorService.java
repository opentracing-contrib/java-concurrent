package io.opentracing.contrib.concurrent;

import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jose Montoya
 *
 * Executor which propagates span from parent thread to scheduled.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 */
public class TracedScheduledExecutorService extends TracedExecutorService implements ScheduledExecutorService {

  private final ScheduledExecutorService delegate;

  public TracedScheduledExecutorService(ScheduledExecutorService delegate, Tracer tracer) {
    this(delegate, tracer, true);
  }

  public TracedScheduledExecutorService(ScheduledExecutorService delegate, Tracer tracer,
      boolean traceWithActiveSpanOnly) {
    super(delegate, tracer, traceWithActiveSpanOnly);
    this.delegate = delegate;
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
    Span span = createSpan("schedule");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.schedule(toActivate == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
    Span span = createSpan("schedule");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.schedule(toActivate == null ? callable :
          new TracedCallable<T>(callable, tracer, toActivate), delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period,
      TimeUnit timeUnit) {
    Span span = createSpan("scheduleAtFixedRate");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.scheduleAtFixedRate(toActivate == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), initialDelay, period, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay,
      TimeUnit timeUnit) {
    Span span = createSpan("scheduleWithFixedDelay");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.scheduleWithFixedDelay(toActivate == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), initialDelay, delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }
}
