# poc-annotation-reflections

Custom annotations read at runtime with reflection, built up step by step. No external libraries.

| Step | Class | What it adds |
|---|---|---|
| 1. Schema | `Main` | declare `@Entity`/`@Column`, read the **metadata** (`getAnnotation`, `getDeclaredFields`) and print the table mapping |
| 2. Values | `FieldValuesMain` | read the **field values** too (`setAccessible` + `field.get`) and build a real `INSERT` |

The annotation is just data; the reflection code is the engine that acts on it.

## Key concepts

- **`@Retention(RUNTIME)`** is mandatory: only runtime-retained annotations survive to reflection. Without it, `getAnnotation` returns `null`.
- **`@Target`** restricts where an annotation may be applied (`TYPE`, `FIELD`, ...).
- **Reading metadata**: `isAnnotationPresent` / `getAnnotation`, then call the elements like methods (`entity.table()`).
- **Reading values**: `field.setAccessible(true)` then `field.get(obj)` reaches private fields at runtime; skip the `setAccessible` and `get` throws `IllegalAccessException`.

## Run

Requires JDK 25 and Maven, no dependencies, no setup beyond that.

```bash
mvn compile exec:java@basics   # step 1: read metadata, print the mapping
mvn compile exec:java@values   # step 2: read values, build an INSERT
```

`mvn compile exec:java` (no `@id`) runs `Main` (step 1).
