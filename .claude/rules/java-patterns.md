---
paths:
  - "src/**/*.java"
---

# Java Patterns

- Use sealed interfaces for type hierarchies (see ActivationFunction, LossFunction, Sample)
- Use records for immutable value types; use Lombok @Builder for complex construction
- Inject dependencies via CDI `@Inject`; fire events via CDI `Event<T>`
- Use MapStruct for object mapping between layers
- Prefer `float[]` over `double[]` for network computations
- Use virtual threads for parallelism (not platform threads)
- Follow Google Java Style (2-space indent, no wildcard imports)
