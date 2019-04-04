package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Pavol Loffay
 *
 * Executor which propagates span from parent thread to submitted.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 */
public class TracedExecutorService extends TracedExecutor implements ExecutorService {

  private final ExecutorService delegate;
  private final boolean traceWithActiveSpanOnly;

  public TracedExecutorService(ExecutorService delegate, Tracer tracer) {
    this(delegate, tracer, true);
  }

  public TracedExecutorService(ExecutorService delegate, Tracer tracer,
      boolean traceWithActiveSpanOnly) {
    super(delegate, tracer, traceWithActiveSpanOnly);
    this.delegate = delegate;
    this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    return delegate.awaitTermination(l, timeUnit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> callable) {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("submit").startActive(true);
    }
    try {
      return delegate.submit(tracer.activeSpan() == null ? callable :
          new TracedCallable<T>(callable, tracer));
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T t) {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("submit").startActive(true);
    }
    try {
      return delegate.submit(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer), t);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public Future<?> submit(Runnable runnable) {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("submit").startActive(true);
    }
    try {
      return delegate.submit(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer));
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection)
      throws InterruptedException {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("invokeAll").startActive(true);
    }
    try {
      return delegate.invokeAll(toTraced(collection));
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l,
      TimeUnit timeUnit) throws InterruptedException {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("invokeAll").startActive(true);
    }
    try {
      return delegate.invokeAll(toTraced(collection), l, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection)
      throws InterruptedException, ExecutionException {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("invokeAny").startActive(true);
    }
    try {
      return delegate.invokeAny(toTraced(collection));
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("invokeAny").startActive(true);
    }
    try {
      return delegate.invokeAny(toTraced(collection), l, timeUnit);
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }

  private <T> Collection<? extends Callable<T>> toTraced(Collection<? extends Callable<T>> delegate) {
    List<Callable<T>> tracedCallables = new ArrayList<Callable<T>>(delegate.size());

    for (Callable<T> callable: delegate) {
      tracedCallables.add(tracer.activeSpan() == null ? callable :
          new TracedCallable<T>(callable, tracer));
    }

    return tracedCallables;
  }
}
