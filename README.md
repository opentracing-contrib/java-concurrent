[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing instrumentation for `java.util.concurrent`
OpenTracing instrumentation for `java.util.concurrent` package. It instruments `ExecutorService`, `Executor`,
`Runnable` and `Callable`.

## Configuration
```java
ExecutorService executorService = new TracedExecutorService(Executors.newFixedThreadPool(4), tracer);
```

## Development
```shell
./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-concurrent.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-concurrent
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-concurrent.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-concurrent
