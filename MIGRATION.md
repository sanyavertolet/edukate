# Java → Kotlin Migration

This document tracks the ongoing migration of Java source files to idiomatic Kotlin across all backend modules.

## Module Status

| Module              | Status         | Java files remaining | Notes                                                            |
|:--------------------|:---------------|---------------------:|:-----------------------------------------------------------------|
| `edukate-auth`      | ✅ Complete     |                    0 | Fully Kotlin                                                     |
| `edukate-gateway`   | ✅ Complete     |                    0 | Fully Kotlin                                                     |
| `edukate-notifier`  | ✅ Complete     |                    0 | Fully Kotlin                                                     |
| `edukate-messaging` | ✅ Complete     |                    0 | Fully Kotlin                                                     |
| `edukate-common`    | 🔄 In progress |                    7 | Security configs, Notifier hierarchy                             |
| `edukate-storage`   | 🔄 In progress |                   11 | Storage abstractions, `FileKey` hierarchy, S3 config             |
| `edukate-checker`   | 🔄 In progress |                   13 | Chat services, Spring AI config, RabbitMQ publisher              |
| `edukate-backend`   | 🔄 In progress |                   41 | Controllers, services, repositories, configs — largest remaining |

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
