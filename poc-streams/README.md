# poc-stream-functional

Demonstrates the four core functional interfaces used with Java Streams:

| Interface | Signature | Stream usage |
|---|---|---|
| `Supplier<T>` | `T get()` | `Stream.generate()`, `Optional.orElseGet()` |
| `Consumer<T>` | `void accept(T t)` | `forEach()`, `peek()` |
| `UnaryOperator<T>` | `T apply(T t)` | `map()` when type stays the same, `List.replaceAll()` |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | `reduce()` |

## Run

```bash
cd poc-stream-functional
mvn compile exec:java
```

## Key concepts

- **Supplier** – a factory/source; produces values without taking any input.
- **Consumer** – a sink; consumes values as side-effects (logging, writing).
- **UnaryOperator** – a pure transformation that keeps the type; composable via `andThen`.
- **BinaryOperator** – a fold/accumulator that collapses two values of the same type into one; used as the reduction step in `reduce()`.
