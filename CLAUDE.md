# edu.yaprnn

Educational GUI application for Multilayer Neural Networks (MLN). Java 26, Gradle 9.4.1, Swing UI.

## Build & Run

```sh
./gradlew build          # compile + test
./gradlew run            # launch GUI (entry: org.jboss.weld.environment.se.StartMain)
./gradlew shadowJar      # fat JAR
./gradlew test           # JUnit 5 tests only
```

JVM: `-XX:+UseZGC` (default). Requires Java 26 toolchain.

## Project Structure

```
src/main/java/edu/yaprnn/
  events/       CDI event routing (MapStruct mappers)
  gui/          Swing UI (61 files): views, editors, panels, services
  model/        Repository pattern — samples, networks, training data
  networks/     Core MLN: MultiLayerNetwork, templates, layers, gradients
  samples/      Sample types: ImageSample, SoundSample, SimpleSample (sealed)
  support/      Swing utilities, Jackson/Random configurers
  training/     TrainingData, DataSelectors (classifier, super-resolution, autoencoder)
src/test/java/  JUnit 5 + AssertJ tests (logic gates: NOT, AND, OR, XOR)
notebooks/      Python Jupyter notebooks (TensorFlow, PyTorch, Flax, scikit-learn)
```

## Architecture

- **CDI (Weld SE)**: dependency injection, event-driven architecture
- **Sealed interfaces**: ActivationFunction (12 impls), LossFunction (4 impls), Sample (3 impls), ModelNode
- **Records + Lombok @Builder**: immutable data objects
- **MapStruct**: event/model mapping
- **Virtual threads**: parallel training (MultiLayerNetwork.learnMiniBatch)
- **Float precision**: all network computations use `float[]`

## Conventions

- Code style: Google Java Style (@intellij-java-google-style.xml)
- Annotation processors: Lombok before MapStruct (via lombok-mapstruct-binding)
- Serialization: Jackson JSON (`.yaprnn-mln`, `.yaprnn-mln-template`, `.yaprnn-training-data`)
- MNIST binary: `.idx3-ubyte` (images), `.idx1-ubyte` (labels)

## Key Patterns

- Event routing: CDI `@Observes` with EventsRouter/EventsMapper
- Repository: singleton `Repository` with grouped lookups, unmodifiable collection views
- Tree model: `NetworksTreeModel` for Swing JTree with lazy-loading node suppliers
- Network creation: `MultiLayerNetworkTemplate` -> `MultiLayerNetwork` (builder pattern)
- Training mode: chunked mini-batch learning (with momentum, L1/L2 regularization)
- Gradient parallelism: batch partitioned into `maxParallelism` chunks, each accumulating in one pre-allocated buffer

## Testing

- Test framework: JUnit 5 (Jupiter) + AssertJ + Mockito 5.23.0 (BOM)
- Coverage: JaCoCo plugin, HTML to `coverage/`, CSV to `build/reports/jacoco/`, `ignoreFailures = true`
- Reproducibility: `TestGradientMatrixService` with seeded `Random`
- Pattern: nested test classes per concern (e.g., `SubSample`, `Accessors`, `QuerySamplesByName`)
- CDI beans: inject mocks via reflection for `@Inject` fields (no CDI container in tests)
- Run: `./gradlew test` (also generates JaCoCo report via `finalizedBy`)
