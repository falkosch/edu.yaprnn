# Feature Specification: Neural Network Persistence System

**Feature Branch**: `[persistence-system]`
**Created**: 2026-02-23
**Status**: Documented
**Input**: User description: "Analyze and document the persistence functionality in the edu.yaprnn project"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Save and Load Trained Networks with Weights (Priority: P1)

As a student learning neural networks, I need to save my trained network (including all learned weights) so that I can resume my work later, share my results with classmates, or compare different training runs without having to retrain from scratch.

**Why this priority**: This is the core value proposition of persistence. Without the ability to save trained networks, users lose all training progress when closing the application. This is the most critical persistence feature as it protects hours of training work.

**Independent Test**: Can be fully tested by training a network on a dataset, saving it to a `.yaprnn-mln` file, closing and reopening the application, loading the saved network, and verifying that predictions match the pre-save state. Delivers immediate value by preserving training investment.

**Acceptance Scenarios**:

1. **Given** a trained MultiLayerNetwork with learned weights, **When** the user selects "File > Save Multilayer Network" and chooses a save location, **Then** the network structure, weights, configuration, and metadata are serialized to a `.yaprnn-mln` file
2. **Given** a `.yaprnn-mln` file exists on disk, **When** the user selects "File > Load Multilayer Network" and selects the file, **Then** the network is deserialized with all weights intact and appears in the Networks tree
3. **Given** a loaded trained network, **When** the user runs inference on the same inputs as before saving, **Then** the predictions are identical to pre-save predictions
4. **Given** the user is saving a network, **When** they provide a filename without the `.yaprnn-mln` extension, **Then** the system automatically appends the correct extension
5. **Given** a save operation completes successfully, **When** the file is written, **Then** a "Finished" dialog is displayed to confirm success

---

### User Story 2 - Save and Load Network Templates (Priority: P2)

As a student experimenting with different network architectures, I need to save network templates (topology without weights) so that I can quickly create multiple network instances with the same architecture for different training experiments, or share architectures with classmates who want to train the same model.

**Why this priority**: Templates enable reproducible experiments and architecture reuse. While less critical than saving trained networks (since templates are faster to recreate), they significantly improve workflow efficiency for architecture experimentation.

**Independent Test**: Can be fully tested by creating a network template with specific layer configurations, saving it to a `.yaprnn-mln-template` file, loading it back, and instantiating a new network from it. Delivers value by enabling rapid architecture prototyping.

**Acceptance Scenarios**:

1. **Given** a MultiLayerNetworkTemplate is selected in the Networks tree, **When** the user selects "File > Save Multilayer Network Template" and chooses a save location, **Then** the template (layer sizes, activation functions, loss function, bias) is serialized to a `.yaprnn-mln-template` file
2. **Given** a `.yaprnn-mln-template` file exists, **When** the user selects "File > Load Multilayer Network Template" and selects the file, **Then** the template is deserialized and appears in the Network Templates section of the tree
3. **Given** a loaded template, **When** the user instantiates a new network from it, **Then** the network has the correct architecture but randomly initialized weights
4. **Given** a template with multiple layers, **When** saved and reloaded, **Then** all layer configurations (size, activation function) are preserved in the correct order
5. **Given** the user saves a template without the `.yaprnn-mln-template` extension, **When** the save dialog completes, **Then** the system automatically appends the correct extension

---

### User Story 3 - Save and Load Training Datasets (Priority: P2)

As a student preparing training data, I need to save training dataset configurations (sample selections, train/test splits) so that I can consistently train different network architectures on the same data partitions, ensuring fair comparisons and reproducible results.

**Why this priority**: Consistent training data is essential for scientific comparison of models. While datasets can be recreated, saving them ensures exact reproducibility of experiments and saves time in dataset preparation.

**Independent Test**: Can be fully tested by creating a training dataset with specific training and dev-test sample selections, saving it to a `.yaprnn-training-data` file, loading it back, and verifying the sample lists match. Delivers value by ensuring reproducible experiments.

**Acceptance Scenarios**:

1. **Given** a TrainingData object with training and dev-test sample selections, **When** the user selects "File > Save Training Data" and chooses a save location, **Then** the dataset configuration (name, sample names, data selector) is serialized to a `.yaprnn-training-data` file
2. **Given** a `.yaprnn-training-data` file exists, **When** the user selects "File > Load Training Data" and selects the file, **Then** the dataset is deserialized and appears in the Training Data section of the tree
3. **Given** a loaded training dataset, **When** used for training, **Then** it references the correct sample names and maintains the train/test split
4. **Given** a dataset with 32,000 training samples and 12,000 test samples, **When** saved and reloaded, **Then** all sample name references are preserved
5. **Given** the user saves training data without the `.yaprnn-training-data` extension, **When** the save dialog completes, **Then** the system automatically appends the correct extension

---

### User Story 4 - Contextual Save Menu Enabling (Priority: P3)

As a user navigating the application, I need the save menu items to be enabled only when I have selected the appropriate object type, so that I avoid confusion about what I can save and don't encounter errors from trying to save the wrong type.

**Why this priority**: This is a usability enhancement that prevents user errors. While important for user experience, the core save/load functionality works without it. Users can still save by selecting the correct node.

**Independent Test**: Can be fully tested by selecting different node types in the Networks tree and verifying that only the appropriate save menu item is enabled. Delivers value by improving UI clarity.

**Acceptance Scenarios**:

1. **Given** the user selects a TrainingDataNode in the Networks tree, **When** viewing the File menu, **Then** only "Save Training Data" is enabled among save options
2. **Given** the user selects a MultiLayerNetworkTemplateNode, **When** viewing the File menu, **Then** only "Save Multilayer Network Template" is enabled among save options
3. **Given** the user selects a MultiLayerNetworkNode or WeightsNode, **When** viewing the File menu, **Then** only "Save Multilayer Network" is enabled among save options
4. **Given** no node is selected or an incompatible node is selected, **When** viewing the File menu, **Then** all save menu items are disabled
5. **Given** the user switches selection between different node types, **When** the selection changes, **Then** the enabled save menu items update immediately

---

### User Story 5 - Error Handling for Corrupted or Incompatible Files (Priority: P3)

As a user loading files, I need clear error messages when a file cannot be loaded due to corruption or incompatibility, so that I understand what went wrong and can take corrective action rather than experiencing silent failures or cryptic errors.

**Why this priority**: Error handling improves user experience but is not core functionality. The system already catches exceptions and shows error dialogs. Enhanced error messages would improve troubleshooting but don't block primary use cases.

**Independent Test**: Can be fully tested by attempting to load corrupted files, wrong file types, or files from incompatible versions, and verifying that meaningful error dialogs appear. Delivers value by helping users diagnose issues.

**Acceptance Scenarios**:

1. **Given** a corrupted `.yaprnn-mln` file, **When** the user attempts to load it, **Then** an error dialog displays with the exception message and the operation is aborted
2. **Given** a file with the wrong extension (e.g., loading a `.yaprnn-mln-template` as a `.yaprnn-mln`), **When** the user attempts to load it, **Then** an error dialog displays indicating deserialization failure
3. **Given** a file from an incompatible version with schema changes, **When** the user attempts to load it, **Then** an error dialog displays indicating the incompatibility
4. **Given** a file that doesn't exist or is inaccessible, **When** the user attempts to load it, **Then** an error dialog displays with the I/O error details
5. **Given** any persistence error occurs, **When** the error dialog is shown, **Then** the error is also logged to the application log at SEVERE level for troubleshooting

---

### User Story 6 - Bundled Resource Loading for Educational Scenarios (Priority: P3)

As an educator or new user, I need to load pre-configured scenarios (like the digits recognition example) from bundled resources so that I can quickly start learning without having to create datasets and networks from scratch.

**Why this priority**: This enhances onboarding and educational value but is not core to the persistence system itself. The "Setup digits scenario" feature demonstrates the system but users can still use all save/load functionality without it.

**Independent Test**: Can be fully tested by selecting "File > Setup digits scenario" and verifying that multiple networks, templates, and training datasets are loaded from bundled resources. Delivers value by providing ready-to-use examples.

**Acceptance Scenarios**:

1. **Given** the user selects "Setup digits scenario", **When** the operation completes, **Then** digit samples, training datasets, network templates, and trained networks are loaded from classpath resources
2. **Given** bundled resources in `src/main/resources`, **When** loading via URL, **Then** the PersistenceService correctly deserializes from classpath URLs
3. **Given** the digits scenario is loaded, **When** viewing the Networks tree, **Then** multiple training datasets, templates, and networks are visible and usable
4. **Given** bundled resource files (`.yaprnn-mln`, `.yaprnn-mln-template`, `.yaprnn-training-data`), **When** loaded from classpath, **Then** they deserialize identically to user-saved files
5. **Given** the digits scenario loads successfully, **When** the user trains or tests the loaded networks, **Then** they function correctly with the loaded training data

---

### Edge Cases

- What happens when the user tries to save to a read-only directory or disk with insufficient space?
- How does the system handle very large network files (e.g., networks with millions of weights)?
- What happens if the file is modified externally while the application is running and then reloaded?
- How does the system handle concurrent access if a file is opened by multiple application instances?
- What happens when loading a file with missing required fields (e.g., a template without a loss function)?
- How does the system handle files with extra fields not recognized by the current version?
- What happens when a training dataset references sample names that don't exist in the current repository?
- How does the system handle network files with NaN or Infinite weight values?
- What happens when the user cancels a file chooser dialog?
- How does the system handle path names with special characters or spaces?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST serialize MultiLayerNetwork objects (including name, layer sizes, activation functions, weights, loss function, and bias) to JSON format in `.yaprnn-mln` files
- **FR-002**: System MUST deserialize MultiLayerNetwork objects from `.yaprnn-mln` files, restoring all network state including trained weights
- **FR-003**: System MUST serialize MultiLayerNetworkTemplate objects (including name, layer templates, loss function, and bias) to JSON format in `.yaprnn-mln-template` files
- **FR-004**: System MUST deserialize MultiLayerNetworkTemplate objects from `.yaprnn-mln-template` files
- **FR-005**: System MUST serialize TrainingData objects (including name, training sample names, dev-test sample names, and data selector) to JSON format in `.yaprnn-training-data` files
- **FR-006**: System MUST deserialize TrainingData objects from `.yaprnn-training-data` files
- **FR-007**: System MUST automatically append the correct file extension if the user omits it during save operations
- **FR-008**: System MUST enable/disable save menu items based on the currently selected node type in the Networks tree
- **FR-009**: System MUST display a "Finished" confirmation dialog after successful save operations
- **FR-010**: System MUST display error dialogs with exception messages when save/load operations fail
- **FR-011**: System MUST log all persistence errors at SEVERE level for troubleshooting
- **FR-012**: System MUST support loading resources from both file system paths and classpath URLs
- **FR-013**: System MUST use Jackson ObjectMapper for all serialization/deserialization with polymorphic type handling via `@class` annotations
- **FR-014**: System MUST preserve object relationships (e.g., a MultiLayerNetwork's reference to its activation functions and loss function)
- **FR-015**: System MUST present file chooser dialogs with appropriate file extension filters for each object type

### Key Entities

- **MultiLayerNetwork**: A trained neural network containing topology (layer sizes, activation functions, loss function, bias), trained weights, and previous gradients. Represents the complete state needed for inference and continued training.
- **MultiLayerNetworkTemplate**: A network architecture specification without weights. Contains layer templates (size and activation function for each layer), loss function, and bias. Used to instantiate new networks with the same topology.
- **TrainingData**: A dataset configuration referencing training and dev-test sample names and a data selector. Links sample names to actual sample objects in the repository.
- **LayerTemplate**: Specification for a single layer in a network template, containing neuron count and activation function.
- **PersistenceService**: Singleton service providing save/load methods for all persistable types, using Jackson ObjectMapper for JSON serialization.
- **FilesService**: Singleton service managing file chooser dialogs and file extension handling, providing FileNameExtensionFilter objects for each file type.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can save a trained network, close the application, reopen it, load the network, and obtain identical predictions on the same inputs (0% prediction difference)
- **SC-002**: All sample `.yaprnn-mln`, `.yaprnn-mln-template`, and `.yaprnn-training-data` files in `src/main/resources` can be successfully loaded without errors
- **SC-003**: Saved files are human-readable JSON format with proper indentation (validated by the Jackson SerializationFeature configuration)
- **SC-004**: Save operations complete within 2 seconds for networks with up to 100,000 weights on standard hardware
- **SC-005**: Load operations complete within 2 seconds for files up to 1MB on standard hardware
- **SC-006**: File extension is correctly appended 100% of the time when users omit it during save
- **SC-007**: Error dialogs provide actionable information in 100% of failure cases (showing exception message)
- **SC-008**: Save menu items correctly enable/disable based on node selection with no false positives or negatives

### Quality Attributes

- **Reliability**: Serialization/deserialization must preserve all data with 100% fidelity (no data loss)
- **Usability**: File chooser dialogs filter to appropriate file types, reducing user confusion
- **Maintainability**: JSON format enables easy inspection and debugging of saved files
- **Interoperability**: JSON format allows potential future integration with other tools or languages
- **Robustness**: System gracefully handles and reports errors for corrupted, incompatible, or missing files

## Technical Architecture *(optional)*

### Core Components

**PersistenceService** (`edu.yaprnn.gui.services.PersistenceService`):
- Singleton service managing all persistence operations
- Injects Jackson ObjectMapper configured by JacksonConfigurer
- Provides save/load methods for each entity type (MultiLayerNetwork, MultiLayerNetworkTemplate, TrainingData)
- All methods annotated with `@SneakyThrows` to propagate exceptions to calling UI layer

**JacksonConfigurer** (`edu.yaprnn.support.JacksonConfigurer`):
- Configures Jackson ObjectMapper with `FAIL_ON_EMPTY_BEANS` disabled
- Produces named bean for dependency injection
- Enables polymorphic type serialization via Jackson annotations

**FilesService** (`edu.yaprnn.gui.services.FilesService`):
- Manages JFileChooser interactions
- Provides FileNameExtensionFilter instances for each file type:
  - `.yaprnn-mln` (Multilayer Network)
  - `.yaprnn-mln-template` (Multilayer Network Template)
  - `.yaprnn-training-data` (Training Data)
- Ensures file extensions are correctly appended

**ControlsService** (`edu.yaprnn.gui.services.ControlsService`):
- Creates save/load menu items with proper icon, title, and file extension filter
- Wraps file selection and persistence operations with try-catch blocks
- Displays success or error dialogs via DialogsService

**MainFrame** (`edu.yaprnn.gui.views.MainFrame`):
- Coordinates UI interactions for save/load operations
- Manages save menu item enabling based on selected node type
- Delegates to PersistenceService for actual persistence operations
- Updates NetworksTreeModel when loading new objects

### File Format Specification

**MultiLayerNetwork (`.yaprnn-mln`)**: JSON with fields:
- `name` (String): Display name
- `layerSizes` (int[]): Neuron count per layer
- `activationFunctions` (ActivationFunction[]): Polymorphic types with `@class` discriminator
- `layerWeights` (float[][]): Trained weight matrices
- `previousLayerGradients` (float[][]): Momentum state
- `lossFunction` (LossFunction): Polymorphic type with `@class` discriminator
- `bias` (float): Bias value

**MultiLayerNetworkTemplate (`.yaprnn-mln-template`)**: JSON with fields:
- `name` (String): Display name
- `layers` (LayerTemplate[]): Array of layer specifications
  - Each LayerTemplate: `size` (int), `activationFunction` (ActivationFunction with `@class`)
- `lossFunction` (LossFunction with `@class`)
- `bias` (float)

**TrainingData (`.yaprnn-training-data`)**: JSON with fields:
- `name` (String): Display name
- `trainingSampleNames` (String[]): References to training samples
- `devTestSampleNames` (String[]): References to test samples
- `dataSelector` (DataSelector with `@class`): Strategy for extracting inputs/targets

### Error Handling Strategy

1. **PersistenceService**: Uses `@SneakyThrows` to propagate checked exceptions (IOException, JsonProcessingException) as unchecked
2. **ControlsService**: Wraps pathConsumer calls in try-catch, catching all Throwable
3. **DialogsService**: Displays error message dialogs and logs at SEVERE level
4. **UI Layer**: User sees error dialog with exception message; operation is aborted; application state remains unchanged

### Data Integrity Mechanisms

- **Type Safety**: Jackson polymorphic deserialization ensures correct type instantiation via `@class` annotations
- **Validation**: Jackson fails fast on unrecognized required fields (though FAIL_ON_EMPTY_BEANS is disabled)
- **Immutability**: Entity classes use Lombok `@Getter` with selective `@Setter` to prevent unintended modifications
- **Atomicity**: File writes are atomic operations; partial writes are handled by Jackson exceptions

## Dependencies & Constraints

### External Dependencies
- **Jackson Databind**: Core serialization library (com.fasterxml.jackson.databind)
- **Jakarta CDI**: Dependency injection framework for service singletons
- **Lombok**: Reduces boilerplate for getters, setters, builders, and exception handling

### Constraints
- File format is JSON text, which may be less efficient than binary for very large networks
- `@SneakyThrows` propagates exceptions as unchecked, requiring careful error handling at call sites
- ObjectMapper configuration is global; changes affect all persistence operations
- Training data references samples by name; samples must exist in repository for dataset to be usable
- No versioning mechanism; schema changes may break compatibility with old files

## Open Questions & Clarifications

1. **File Format Versioning**: Should we add a version field to support schema evolution and backward compatibility?
2. **Large File Handling**: Should we implement streaming serialization for networks with millions of weights?
3. **Validation on Load**: Should we validate loaded networks (e.g., check for NaN weights, layer size consistency)?
4. **Auto-save**: Should we implement auto-save functionality for in-progress training sessions?
5. **Export Formats**: Should we support exporting to other formats (ONNX, TensorFlow SavedModel) for interoperability?
6. **Compression**: Should we compress `.yaprnn-mln` files to reduce disk space for large networks?
7. **File Locking**: Should we implement file locking to prevent concurrent modifications?
8. **Sample Resolution**: What should happen when loading training data with sample names that don't exist in the repository?

## Implementation Notes

### Current Implementation Status
The persistence system is fully implemented and operational. This specification documents the existing functionality.

### Sample Files
The project includes comprehensive sample files in `src/main/resources/`:
- **Networks**: `digits.yaprnn-mln`, `digits-image-from-label.yaprnn-mln`, etc.
- **Templates**: `digits.yaprnn-mln-template`, `digits-super-resolution-layers-3.yaprnn-mln-template`, etc.
- **Training Data**: `digits.yaprnn-training-data`, `vowels.yaprnn-training-data`, etc.

### Integration Points
- **NetworksTreeModel**: Adds loaded objects to the tree structure for UI display
- **Repository**: Stores samples that training data references by name
- **GradientMatrixService**: Used when instantiating networks from templates (initializes weights)
- **Event Routers**: Track currently selected nodes to determine which save operations are available

### Extension Points
- **New Entity Types**: Add save/load methods to PersistenceService and corresponding menu items to MainFrame
- **File Format Plugins**: Could implement alternative serialization strategies by injecting different ObjectMapper configurations
- **Custom Dialogs**: Could replace generic DialogsService with specialized save/load progress dialogs
