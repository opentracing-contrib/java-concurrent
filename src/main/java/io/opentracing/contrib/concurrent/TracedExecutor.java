package io.opentracing.contrib.concurrent;

import java.util.concurrent.Executor;

import io.opentracing.Tracer;

/**
 * Executor which propagates span from parent thread to submitted {@link Runnable}.
 *
 * @author Pavol Loffay
 */
public class TracedExecutor implements Executor {

  protected final Tracer tracer;
  private final Executor delegate;

  public TracedExecutor(Executor executor, Tracer tracer) {
    this.delegate = executor;
    this.tracer = tracer;
  }

  @Override
  public void execute(Runnable runnable) {
    delegate.execute(new TracedRunnable(runnable, tracer.activeSpan()));
  }
}
