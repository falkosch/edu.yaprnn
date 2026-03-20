---
paths:
  - "src/**/*.java"
---

# Code Quality

- Files should not be bigger than 500 lines.
- Avoid preemptive abstraction with only low benefit.
- Use concise strategic comments.
- Use strategic logging for debugging.
- Prefer composition to inheritance.

## Java Patterns

- Not more than 50 lines per method.
- Ideally, one class or record per file.
- Use sealed interfaces for type hierarchies
- Prefer immutable objects, e.g. records or value objects.
- Use Lombok @Builder for complex construction
- Prefer lombok annotations over boilerplate code.
- Use MapStruct for object mapping between layers
- Inject dependencies via CDI `@Inject`
- Fire events via CDI `Event<T>`
- Prefer `float[]` over `double[]` for network computations
- Use virtual threads for parallelism (not platform threads)
- Follow Google Java Style (2-space indent, no wildcard imports)

# Test Quality

- Near 100% branch coverage wanted. GUI packages are exempt from this.
- Prefer simple tests following the Arrange-Act-Assert pattern.
- Use Mockito for mocking, AssertJ for assertions.
- Avoid abstractions in test code.
