package io.opentracing.contrib.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class TracedExecutorService extends TracedExecutor implements ExecutorService {

  private final ExecutorService delegate;

  public TracedExecutorService(ExecutorService delegate, Tracer tracer) {
    super(delegate, tracer);
    this.delegate = delegate;
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
    return delegate.submit(new TracedCallable<T>(callable, tracer.activeSpan()));
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T t) {
    return delegate.submit(new TracedRunnable(runnable, tracer.activeSpan()), t);
  }

  @Override
  public Future<?> submit(Runnable runnable) {
    return delegate.submit(new TracedRunnable(runnable, tracer.activeSpan()));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection)
      throws InterruptedException {
    return delegate.invokeAll(toTraced(collection));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l,
      TimeUnit timeUnit) throws InterruptedException {
    return delegate.invokeAll(toTraced(collection), l, timeUnit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection)
      throws InterruptedException, ExecutionException {
    return delegate.invokeAny(toTraced(collection));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return delegate.invokeAny(toTraced(collection), l, timeUnit);
  }

  private <T> Collection<? extends Callable<T>> toTraced(Collection<? extends Callable<T>> delegate) {
    List<Callable<T>> tracedCallables = new ArrayList<Callable<T>>(delegate.size());

    for (Callable<T> callable: delegate) {
      tracedCallables.add(new TracedCallable<T>(callable, tracer.activeSpan()));
    }

    return tracedCallables;
  }
}
