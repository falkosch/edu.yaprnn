# edu.yaprnn Constitution

## Core Principles

### Modular Architecture
The application maintains clear separation between neural network core (networks, functions, samples, training packages) and GUI layer (gui, events, model, support packages). Core network functionality must remain independent of GUI implementation to enable potential future architectural changes while maintaining testability and reusability.

**Boundaries:**
- `networks`, `functions`, `samples`, `training` packages contain pure computational logic with no GUI dependencies
- `gui`, `events`, `model`, `support` packages handle presentation and user interaction
- Communication flows through well-defined boundaries using dependency injection (Weld CDI)
- Event-driven architecture decouples GUI components via CDI events

**Rationale:** This enables testing network algorithms independently, supports future refactoring (e.g., web interface, CLI tools), and maintains educational clarity by separating domain logic from presentation.

### Test-driven Development (NON-NEGOTIABLE)
All changes to core network functionality require tests written before implementation.

GUI components require manual testing scenarios documented in specifications.

**Test Creation Order:**
1. Integration tests (realistic dependencies)
2. Unit tests (implementation details)

**Non-Negotiable Requirements:**
- **Unit tests**: Required for all activation functions, loss functions, weight initialization, and mathematical computations
- **Integration tests**: Required for network training workflows, sample data loading, classification pipelines
- **Test-driven workflow in fast iterations:** Implement → Write tests → Verify new tests and existing ones pass → Repeat until satisfied
- **GUI changes:** Document acceptance scenarios in specifications; manual testing is acceptable given JSwing's testing limitations
- **Consider the computational nature of Neural Networks:** Numerical stability and floating-point rounding errors in tests may cause false negatives as well as false positives. Double-check test reports and test code.

**Exceptions:** Simple GUI layout adjustments, visual styling, and icon updates may proceed without formal tests if they don't affect computational behavior.

**Rationale:** Machine learning algorithms are inherently complex and error-prone. Tests serve as executable documentation and prevent regression in mathematical correctness. Educational tool quality depends on computational accuracy.

### Specification-Driven Features
New features and significant changes require specifications following spec-kit methodology. Specifications must define user scenarios, requirements, success criteria, and key entities before implementation begins.

**Process:**
1. Create feature specification in `.specify/specs/[feature-name]/spec.md`
2. Document user stories with priority (P1-P3) and acceptance scenarios
3. Define functional requirements, key entities, and success criteria
4. Review specification before implementation
5. Implementation follows specification; deviations require spec updates

**What requires specs:**
- New neural network capabilities (new activation functions, training algorithms, architectures)
- Changes to existing training, classification, or data management workflows
- New GUI features that expose new domain functionality

**What doesn't require specs:**
- Bug fixes that restore documented behavior
- Performance optimizations maintaining existing interfaces
- Refactoring without behavioral changes

**Rationale:** Educational software requires clear documentation of expected behavior. Specs serve as learning materials and ensure features meet pedagogical goals.

### IV. Educational Transparency
The application prioritizes educational value through visualization, inspection capabilities, and observable behavior. Users must be able to inspect internal network states, visualize data transformations, and understand algorithmic decisions.

**Requirements:**
- Layer-by-layer activation visualization during classification
- Weight matrix visualization (numerical and heat map)
- Training progress visualization with error/accuracy plots
- Clear mathematical notation in UI (e.g., "Sigmoid: 1 / (1 + exp[-v])")
- Configurable visualization parameters (zoom, gamma correction)

**Rationale:** The primary purpose is education. Transparency enables students to understand neural network internals, not just use them as black boxes.

### V. Stable Core, Flexible Presentation
Core network algorithms (activation functions, weight initialization, training algorithms) prioritize correctness and stability. GUI components may evolve more rapidly to improve usability without affecting core behavior.

**Core stability rules:**
- Mathematical correctness is non-negotiable
- Breaking changes to network persistence formats require migration paths
- Activation function, loss function interfaces remain stable
- Weight initialization algorithms maintain deterministic behavior (given same seed)

**GUI flexibility:**
- Layout, controls, and visualization may change to improve UX
- New visualization options may be added without core changes
- GUI events and model nodes may be refactored as needed

**Rationale:** Students' saved networks must remain loadable across versions. Mathematical algorithms require careful validation. GUI improvements enhance learning without affecting correctness.

## Technology Stack

### Required Technologies
- **Java 21+**: Language runtime (currently using Java 25)
- **JSwing**: GUI framework for desktop application
- **Gradle**: Build system and dependency management
- **JUnit 5**: Unit and integration testing framework
- **AssertJ**: Fluent assertion library for tests
- **Weld CDI**: Dependency injection and event management
- **Jackson**: JSON serialization for persistence
- **Lombok**: Boilerplate reduction (getters, builders, etc.)
- **MapStruct**: Type-safe mapping between layers

### Libraries
- **JTransforms**: FFT operations for audio samples
- **JFreeChart**: Training progress visualization

### Constraints
- No E2E testing framework currently available; rely on unit/integration tests and manual GUI testing
- Desktop-only application; no web or mobile interfaces planned (but not prohibited)
- Single-user local application; no multi-user or server requirements

## Development Workflow

### Change Process
1. **For core network features**: Write specification → Write tests → Implement → Verify tests pass
2. **For GUI features**: Write/update specification → Implement → Manual testing against acceptance scenarios
3. **For bug fixes**: Write failing test demonstrating bug → Fix → Verify test passes

### Code Quality Standards
- All code must compile without warnings
- Core network code must have >80% test coverage
- Use dependency injection; avoid static dependencies where possible
- Follow existing package organization (networks, gui, events, model, support)
- Mathematical operations should include comments explaining algorithms

### Persistence and Compatibility
- Network templates, network instances, training data, and samples persist in JSON format via Jackson
- Breaking changes to persisted formats require:
  - Version field in serialized format
  - Migration code to load previous versions
  - Documentation of changes
- Default to backward compatibility; prefer additive changes

## Governance

This constitution governs architectural decisions, development practices, and quality standards for edu.yaprnn. It supersedes informal practices and preferences.

**Enforcement:**
- All specifications must reference relevant constitutional principles
- Code reviews verify compliance with test-first requirements
- Breaking changes to core network APIs require explicit justification
- Deviations from test-first principle require documented rationale

**Amendment Process:**
- Constitutional changes require updates to this document
- Major changes should be discussed before implementation
- Amendments should document rationale and affected systems

**Living Document:**
- This constitution evolves with the project
- When patterns emerge that should be standardized, add them here
- Review constitution periodically during major feature development

**Version**: 1.0.0 | **Ratified**: 2026-02-23 | **Last Amended**: 2026-02-23
