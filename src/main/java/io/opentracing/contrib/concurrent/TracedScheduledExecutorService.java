package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
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
    Scope scope = createScope("schedule");
    try {
      return delegate.schedule(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer), delay, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
    Scope scope = createScope("schedule");
    try {
      return delegate.schedule(tracer.activeSpan() == null ? callable :
          new TracedCallable<T>(callable, tracer), delay, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period,
      TimeUnit timeUnit) {
    Scope scope = createScope("scheduleAtFixedRate");
    try {
      return delegate.scheduleAtFixedRate(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer), initialDelay, period, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay,
      TimeUnit timeUnit) {
    Scope scope = createScope("scheduleWithFixedDelay");
    try {
      return delegate.scheduleWithFixedDelay(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer), initialDelay, delay, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}