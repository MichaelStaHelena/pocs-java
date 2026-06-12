# poc-concurrency

Java concurrency, from the classic primitives to the modern high-level layer: four runnable demos.

| Topic | Class | What it shows |
|---|---|---|
| ExecutorService | `ExecutorServiceMain` | thread pools (fixed, single, cached, virtual, scheduled), `execute`/`submit`, shutdown lifecycle |
| Callable & Future | `CallableFutureMain` | async results, `get`/timeout/cancel, `ExecutionException`, `invokeAll`/`invokeAny` |
| Locks & Atomics | `LocksAtomicMain` | race condition, `synchronized`, `ReentrantLock`, `tryLock`, `Condition`, `ReentrantReadWriteLock`, `AtomicInteger`, CAS |
| Modern (stable) | `ModernConcurrencyMain` | `CompletableFuture` pipelines (`thenCompose`/`thenCombine`/`allOf`/`exceptionally`), `ConcurrentHashMap`, `LongAdder`, `BlockingQueue` |

The first three answer different questions: **how to run** concurrent tasks, **how to collect their results**, and **how to keep shared state correct**. The fourth, `ModernConcurrencyMain`, is what you actually write *on top of* them today.

## When to reach for each

### ExecutorService: run many tasks without managing threads by hand

A `new Thread()` per task is expensive and unbounded. Open 10k of them and the box falls over. A pool reuses a handful of threads, caps how many run at once, and shuts down cleanly. It separates *what* to run from *how* the threads are managed.

- **Server / API**: each incoming request runs on a pool thread (what every servlet container does underneath).
- **Batch work**: resize 5k images, import a huge CSV, send 800 emails, hand it all to the pool and it spreads the load.
- **Scheduled jobs** (`ScheduledExecutorService`): health check every 30s, hourly cache cleanup, queue polling.
- **Virtual threads** (`newVirtualThreadPerTaskExecutor`): thousands of tasks that mostly *wait* on network or DB. Virtual threads are cheap, so 50k connections blocked on I/O fit in memory, impossible with platform threads.

### Callable & Future: when a background task returns a value

`Runnable` returns nothing; `Callable` returns a value (and may throw). The `Future` is the receipt: fire the task, carry on, and block only when you actually need the result.

- **Fan-out / fan-in**: fire 3 external calls in parallel, then collect the 3 answers. Three 200ms calls cost ~200ms instead of 600ms.
- **Timeout**: call a slow service and *give up* after 2s (`get` with timeout) instead of hanging forever.
- **Cancellation**: the user closes the screen or restarts the search → cancel the in-flight task.
- **`invokeAny`**: "first to answer wins", query three mirrors/replicas, keep the fastest.
- **`invokeAll`**: wait for a whole set of tasks to finish before moving on.

A task's exception is stashed and only surfaces on `get()` (wrapped in `ExecutionException`). You choose where to handle it.

### Locks & Atomics: protect state that several threads touch

The moment two threads write the same field without coordination you get a race: the `198862` instead of `400000` in the demo. This is about **correctness**, not speed.

- **Atomic** (`AtomicInteger` & friends): a single variable, counter, flag, accumulator. Requests served, sequential IDs, a metric. Lock-free, faster, and covers most "I just need a safe counter" cases.
- **ReentrantLock**: guard a *block* of operations that must happen together. A bank transfer is the classic: debit one account and credit another with nothing slipping in between. Adds what `synchronized` lacks: `tryLock` (try without blocking, dodges deadlock), timed lock, and `Condition` (await/signal) for coordination like a bounded producer/consumer buffer.
- **ReentrantReadWriteLock**: data read a lot, written rarely. Many readers share access; only writes serialize. An in-memory cache or config everyone reads and almost no one updates, far more throughput than a plain lock that would make readers wait on each other.

**How they fit together:** you **run** tasks on the `ExecutorService`, **collect** their results through `Future`, and when those tasks **share** memory, you guard it with a lock or an atomic.

### Modern: compose on top instead of hand-rolling

`ModernConcurrencyMain` is the sibling layer: the same concurrency, at the level you actually code at day-to-day.

- **CompletableFuture**: chain and merge async steps (`thenCompose`, `thenCombine`, `allOf`) and handle failures (`exceptionally`) without a blocking `get()` in the middle of the pipeline.
- **Concurrent collections**: `ConcurrentHashMap.merge` for atomic updates, `LongAdder` for counters under contention, and `BlockingQueue` as a ready-made producer/consumer (the `Condition` dance from `LocksAtomicMain`, done for you).
- **StructuredTaskScope** is intentionally left out. It's still a *preview* API in Java 25, so adding it would force `--enable-preview` on the whole module. It moves in once it stabilizes.

## Run

Requires JDK 25 and Maven. No other dependencies to install.

```bash
cd poc-concurrency
mvn compile exec:java@executor   # ExecutorService
mvn compile exec:java@future     # Callable & Future
mvn compile exec:java@locks      # Locks & Atomics
mvn compile exec:java@modern     # CompletableFuture + concurrent collections
```

`mvn compile exec:java` (no `@id`) runs `ExecutorServiceMain` by default.

## Implementation notes

- `ExecutorService` is `AutoCloseable` (Java 19+): try-with-resources calls `close()`, which waits for tasks to finish.
- A task's failure is wrapped in `ExecutionException`, thrown only when you call `Future.get()`.
- Always `unlock()` in a `finally`, or an exception leaves the lock held forever.
- `Condition.await()` releases the lock and sleeps until a `signal()`. Always wait in a `while`, never an `if`, since the condition can change before the lock is reacquired.
- `AtomicInteger` updates are lock-free via CAS (`compareAndSet`): retry-on-conflict instead of blocking.
- `CompletableFuture` wraps a task's failure in `CompletionException`. Unwrap it with `getCause()` inside `exceptionally`/`handle`.
