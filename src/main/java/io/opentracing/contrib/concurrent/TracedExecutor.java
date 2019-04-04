package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import java.util.concurrent.Executor;

/**
 * Executor which propagates span from parent thread to submitted {@link Runnable}.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 *
 * @author Pavol Loffay
 */
public class TracedExecutor implements Executor {

  protected final Tracer tracer;
  private final Executor delegate;
  private final boolean traceWithActiveSpanOnly;

  public TracedExecutor(Executor executor, Tracer tracer) {
    this(executor, tracer, true);
  }

  public TracedExecutor(Executor executor, Tracer tracer, boolean traceWithActiveSpanOnly) {
    this.delegate = executor;
    this.tracer = tracer;
    this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
  }

  @Override
  public void execute(Runnable runnable) {
    Scope scope = null;
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      scope = tracer.buildSpan("execute").startActive(true);
    }
    try {
      delegate.execute(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer));
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
