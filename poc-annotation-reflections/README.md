# poc-annotation-reflection

Define a custom annotation and read it back at runtime with reflection: a minimal mini-ORM mapper, no external libraries.

`Main` declares two annotations (`@Entity` on a class, `@Column` on fields), puts them on a `User` class, then walks the class with reflection and prints the mapping. The annotation is just data; the reflection code is the engine that acts on it.

## Key concepts

- **`@Retention(RUNTIME)`** is mandatory: only runtime-retained annotations survive to reflection. Without it, `getAnnotation` silently returns `null`.
- **`@Target`** restricts where an annotation may be applied (`TYPE`, `FIELD`, ...).
- **Reading**: `isAnnotationPresent(X.class)` checks presence, `getAnnotation(X.class)` returns the instance, then you call its elements like methods (`entity.table()`).
- `getDeclaredFields()` lets the engine react to whatever fields carry `@Column`, without hard-coding the class.

## Run

Requires JDK 25 and Maven, no dependencies, no setup beyond that.

```bash
mvn compile exec:java
```
