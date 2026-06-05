# Functional Interfaces POC

Notes from exploring `java.util.function`. Reference for the common ones.

## Quick reference

### `Predicate<T>`

Checks something and returns a boolean. Supports `.and()`, `.or()`, `.negate()`.

```java
Predicate<String> startsWithA = s -> s.startsWith("A");
startsWithA.test("Audi");           // true
startsWithA.negate().test("BMW");   // true
```

### `Function<T, R>`

Maps one value to another. Supports `.andThen()` and `.compose()` for chaining.

```java
Function<String, Integer> length = String::length;
Function<Integer, String> label = n -> "length: " + n;
length.andThen(label).apply("Audi"); // "length: 4"
```

### `Consumer<T>`

Takes a value and does something with it. No return value.

```java
Consumer<String> printer = System.out::println;
printer.accept("hello");
```

### `Supplier<T>`

Returns a value, takes no input. Useful for defaults and lazy init.

```java
Supplier<String> fallback = () -> "guest";
String user = currentUser != null ? currentUser : fallback.get();
```

### `UnaryOperator<T>`

Like `Function<T, T>` — input and output are the same type.

```java
UnaryOperator<String> normalize = s -> s.trim().toLowerCase().replace(" ", "_");
```

### `BiFunction<T, U, R>`

Two inputs, one output.

```java
BiFunction<String, Integer, String> version = (name, v) -> name + " v" + v;
```

### `BinaryOperator<T>`

Like `BiFunction<T, T, T>`. Often used with `Stream.reduce()`.

```java
BinaryOperator<String> join = (a, b) -> a + ", " + b;
tags.stream().reduce(join); // "sport, awd, turbo"
```

## `@FunctionalInterface`

Optional annotation — makes the compiler enforce that the interface has exactly one abstract method.

## In this project

Demo in [src/main/java/com/michael/poc/Main.java](src/main/java/com/michael/poc/Main.java).  
Two sections: core (basic usage per interface) and advanced (composition + streams).

Didn't cover primitive variants (`IntPredicate`, `IntFunction`, etc.) — same idea, just avoids boxing.

## Run

```bash
mvn exec:java
```
