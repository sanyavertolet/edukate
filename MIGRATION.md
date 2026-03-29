# Java → Kotlin Migration

This document tracks the ongoing migration of Java source files to idiomatic Kotlin across all backend modules.

## Module Status

| Module              | Status         | Java files remaining | Notes                                                                 |
|:--------------------|:---------------|---------------------:|:----------------------------------------------------------------------|
| `edukate-auth`      | ✅ Complete     |                    0 | Fully Kotlin                                                          |
| `edukate-gateway`   | ✅ Complete     |                    0 | Fully Kotlin                                                          |
| `edukate-notifier`  | ✅ Complete     |                    0 | Fully Kotlin                                                          |
| `edukate-messaging` | ✅ Complete     |                    0 | Fully Kotlin                                                          |
| `edukate-common`    | ✅ Complete     |                    0 | Fully Kotlin                                                          |
| `edukate-storage`   | ✅ Complete     |                    0 | Fully Kotlin; `FileKey` → sealed class; `@PersistenceCreator` removed |
| `edukate-checker`   | ✅ Complete     |                    0 | Fully Kotlin; Spring AI, RabbitMQ, S3; see `TECH_DEBT.md` for notes   |
| `edukate-backend`   | 🔄 In progress |                   41 | Controllers, services, repositories, configs — largest remaining      |

## Migration Guidelines

### Lombok removal

Java files in this project use Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.).
Remove Lombok annotations when migrating — Kotlin data classes and primary constructors make them unnecessary.

### Spring annotations

- `@Component`, `@Service`, `@RestController`, `@Configuration` carry over unchanged.
- Constructor injection: replace `@Autowired` / `@RequiredArgsConstructor` with a Kotlin primary constructor.
  Spring auto-detects single-constructor injection — no annotation needed.
- `@Value("\${property}")` — use `@param:Value("\${property}")` on constructor parameters to target the
  constructor parameter rather than the backing field.

### Reactive types

- Return `Mono<T>` / `Flux<T>` from all service and controller methods — never block.
- Replace `return Mono.just(x).map { ... }` chains with `reactor-kotlin-extensions` operators where they
  read more naturally (e.g., `x.toMono()`, `list.toFlux()`).
- `Optional` → nullable Kotlin types (`T?`); replace `.orElse(null)` with `?: null` or just nullability.

### Null safety

- Remove `@NonNull` / `@Nullable` JSR-305 annotations — Kotlin's type system handles this.
- Replace `Objects.requireNonNull(x)` with `requireNotNull(x)` or `checkNotNull(x)`.

### Data classes

- Java POJOs and DTOs → Kotlin `data class`. Equals, hashCode, copy, and toString are generated automatically.
- Entities (MongoDB documents) should be `data class` only if they have stable identity semantics;
  otherwise a plain `class` with explicit `equals`/`hashCode` on the `@Id` field is safer.

### Interfaces and abstract classes

- Java interfaces with default methods → Kotlin interfaces with default implementations (identical syntax).
- Abstract classes with only abstract methods → consider converting to a Kotlin `interface`.

## Suggested Migration Order

1. **`edukate-common`** — small, no Spring Boot runtime dependency; unblocks downstream modules.
2. **`edukate-storage`** — self-contained library; `FileKey` hierarchy maps naturally to a Kotlin sealed class.
3. **`edukate-checker`** — medium size; Spring AI config is straightforward to convert.
4. **`edukate-backend`** — largest module; migrate layer by layer (repositories → services → controllers).

---

## edukate-storage Migration Notes

### Build configuration

Replace the `java` plugin block with full Kotlin setup:

```diff
 plugins {
-    java
+    kotlin("jvm")
+    kotlin("plugin.spring")
     alias(libs.plugins.spring.boot)
     alias(libs.plugins.spring.dependency.management)
+    id("io.github.sanyavertolet.edukate.buildutils.kotlin-quality-configuration")
 }

-java {
-    toolchain {
-        languageVersion = JavaLanguageVersion.of(21)
-    }
-}
+kotlin {
+    jvmToolchain(21)
+}

 dependencies {
     ...
-    compileOnly(libs.lombok)
-    compileOnly(libs.fasterxml.jackson.annotations)
-    annotationProcessor(libs.lombok)
     annotationProcessor(libs.spring.boot.configuration.processor)
+    implementation(libs.kotlin.reflect)
+    implementation(libs.reactor.kotlin.extensions)
+
+    testImplementation("org.springframework.boot:spring-boot-starter-test")
+    testImplementation("io.projectreactor:reactor-test")
+    testImplementation(libs.mockk)
+    testImplementation(libs.springmockk)
 }
+
+tasks.withType<Test>().configureEach {
+    useJUnitPlatform()
+}
```

`kotlin("plugin.spring")` automatically makes `@Configuration`, `@Component`, and `@Bean`
classes/methods `open`, removing the need for manual `open` keywords.
Jackson annotations (`@JsonTypeInfo`, `@JsonSubTypes`, etc.) are already on to compile classpath
transitively — no additional dependency needed.

### Conversion order (leaf-first)

Convert files in this order to avoid forward references at each step:

| #  | File                           | Key transformation                          |
|----|--------------------------------|---------------------------------------------|
| 1  | `ReadOnlyStorage.java`         | Interface — direct syntax conversion        |
| 2  | `Storage.java`                 | Interface with default method               |
| 3  | `configs/S3Properties.java`    | `@Data` → `open class` with `lateinit var`  |
| 4  | `configs/S3Config.java`        | `@AllArgsConstructor` → primary constructor |
| 5  | `keys/ProblemFileKey.java`     | Custom `equals`/`hashCode` fix (see below)  |
| 6  | `keys/ResultFileKey.java`      | Same pattern as ProblemFileKey              |
| 7  | `keys/TempFileKey.java`        | Same pattern                                |
| 8  | `keys/SubmissionFileKey.java`  | Same pattern                                |
| 9  | `keys/FileKey.java`            | **`abstract class` → `sealed class`**       |
| 10 | `AbstractReadOnlyStorage.java` | `@Slf4j` → companion object logger          |
| 11 | `AbstractStorage.java`         | Remove spurious `import java.awt.*`         |

### Per-file notes

#### `ReadOnlyStorage.java` → `ReadOnlyStorage.kt`

Direct conversion. Kotlin interfaces support default methods identically. No behavioural change.

#### `Storage.java` → `Storage.kt`

The default `upload` method computes total byte size before delegating:

```java
// Java
buffers.stream().

mapToInt(ByteBuffer::remaining).

sum()
```

```kotlin
// Kotlin
buffers.sumOf { it.remaining() }
```

#### `configs/S3Properties.java` → `configs/S3Properties.kt`

`@Data` + `@ConfigurationProperties` → `open class` with `lateinit var`.
Spring's property binding requires setters, so `data class` (with `val`) is not suitable here.
`Duration` is a reference type and supports `lateinit var`.

```kotlin
@ConfigurationProperties(prefix = "s3")
open class S3Properties {
    lateinit var endpoint: String
    lateinit var region: String
    lateinit var accessKey: String
    lateinit var secretKey: String
    lateinit var bucket: String
    lateinit var signatureDuration: Duration
}
```

#### `configs/S3Config.java` → `configs/S3Config.kt`

`@AllArgsConstructor` → primary constructor. `@Bean` methods become `open fun` automatically
via `kotlin("plugin.spring")`. Property getters (`getAccessKey()`, etc.) become direct property
access (`s3Properties.accessKey`).

#### `keys/ProblemFileKey.java` (and Result, Temp, Submission) → `.kt`

**`@EqualsAndHashCode` bug fix:** All four key subclasses use
`@EqualsAndHashCode(callSuper = true, of = {"someId"})`. The `callSuper = true` flag invokes
`Object.equals()` on the parent, meaning two separately constructed instances with identical IDs
are never equal. This is almost certainly unintentional.

Fix: implement `equals`/`hashCode` based only on the identity fields, without `super.equals()`.

```kotlin
class ProblemFileKey @PersistenceCreator constructor(
    val problemId: String,
    fileName: String,
) : FileKey(fileName) {

    override fun equals(other: Any?) = other is ProblemFileKey && problemId == other.problemId
    override fun hashCode() = problemId.hashCode()
    override fun toString() = "problems/$problemId/$fileName"

    companion object {
        fun of(problemId: String, fileName: String) = ProblemFileKey(problemId, fileName)
        fun prefix(problemId: String) = "problems/$problemId/"
    }
}
```

Same pattern for `ResultFileKey` (identity: `problemId`), `TempFileKey` (identity: `userId`),
and `SubmissionFileKey` (identity: `userId + problemId + submissionId`).

#### `keys/FileKey.java` → `keys/FileKey.kt` — sealed class

This is the most significant transformation. The abstract Java class with `instanceof` chains
maps directly to a Kotlin `sealed class` with exhaustive `when` expressions.

```kotlin
// Java
if (key instanceof TempFileKey) return "tmp";
if (key instanceof SubmissionFileKey) return "submission";
...
return "base";

// Kotlin — exhaustive, no else branch, compiler-verified
fun typeOf(key: FileKey) = when (key) {
    is TempFileKey -> "tmp"
    is SubmissionFileKey -> "submission"
    is ProblemFileKey -> "problem"
    is ResultFileKey -> "result"
}
```

`ownerOf()` becomes a `when` expression returning `String?`:

```kotlin
fun ownerOf(key: FileKey): String? = when (key) {
    is TempFileKey -> key.userId
    is SubmissionFileKey -> key.userId
    is ProblemFileKey -> null
    is ResultFileKey -> null
}
```

The `of(rawKey)` factory keeps identical parsing logic. The Java stream chain:

```java
Arrays.stream(norm.split("/")).

filter(s ->!s.

isEmpty()).

toArray(String[]::new)
```

becomes:

```kotlin
norm.split("/").filter { it.isNotEmpty() }
```

Keep `@JsonTypeInfo` and `@JsonSubTypes` on the sealed class — Jackson still needs them for
polymorphic deserialization. The `@JsonTypeName("base")` on the abstract parent and
`@JsonTypeName(...)` on each subclass are preserved exactly.

#### `AbstractReadOnlyStorage.java` → `AbstractReadOnlyStorage.kt`

`@Slf4j` → companion object logger. The `protected final` fields become `protected val` in
the primary constructor. Lambda improvements:

```java
// Java
.onErrorResume(S3Exception .class, e ->e.

statusCode() ==404?Mono.

empty() :Mono.

error(e))
        Mono.

fromFuture(() ->s3AsyncClient.

getObject(request, ...))
```

```kotlin
// Kotlin
.onErrorResume(S3Exception::class.java) { e -> if (e.statusCode() == 404) Mono.empty() else Mono.error(e) }
Mono.fromFuture { s3AsyncClient.getObject(request, ...) }
```

#### `AbstractStorage.java` → `AbstractStorage.kt`

- Remove spurious `import java.awt.*` (unused, likely an IDE accident).
- `.onErrorResume(_ -> Mono.just(false))` → `.onErrorResume { Mono.just(false) }`
- `.map(_ -> key)` → `.thenReturn(key)` (more idiomatic when the mapped value is ignored)
- `.flatMap(_ -> delete(source))` → `.flatMap { delete(source) }`
- Java stream to build `ObjectIdentifier` list:

```java
// Java
keys.stream().

map(key ->ObjectIdentifier.

builder().

key(key.toString()).

build()).

toList()
```

```kotlin
// Kotlin
keys.map { ObjectIdentifier.builder().key(it.toString()).build() }
```
