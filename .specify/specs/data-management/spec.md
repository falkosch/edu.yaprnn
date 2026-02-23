# Feature Specification: Training Data Management

**Feature Branch**: `data-management`
**Created**: 2026-02-23
**Status**: Analysis
**Input**: User description: "Analyze the data management functionality in the edu.yaprnn project"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Training Dataset with Train/Test Split (Priority: P1)

As a neural network student, I want to create a training dataset from my loaded samples with a configurable train/test split, so that I can separate my data for training and validation purposes while experimenting with different neural network architectures.

**Why this priority**: This is the foundational capability for all supervised learning experiments. Without the ability to create and partition training data, users cannot train neural networks or evaluate their performance. This is the most critical user journey that must work for any educational value to be delivered.

**Independent Test**: Can be fully tested by loading samples into the application, clicking "Randomized Training Data" button, specifying a name and train/test split percentages (e.g., 70% training, 30% test), selecting a data selector, and verifying the created training dataset appears in the tree with correct sample counts and percentage distributions.

**Acceptance Scenarios**:

1. **Given** 100 samples are loaded in the repository, **When** user creates a training dataset with 70% training and 30% test split, **Then** the dataset contains approximately 70 training samples and 30 test samples
2. **Given** the user is creating a new training dataset, **When** user enters a custom name "MNIST Digits Experiment 1", **Then** the dataset is created with that exact name and appears in the Training Data list
3. **Given** multiple samples exist in the repository, **When** user creates a training dataset, **Then** samples are randomly shuffled before partitioning to ensure unbiased distribution
4. **Given** user specifies training percentage of 80% and test percentage of 40%, **When** creating the dataset, **Then** the system normalizes percentages so the sum equals 100% (66.67% training, 33.33% test)
5. **Given** a training dataset has been created, **When** user expands the dataset node in the tree, **Then** they see "Training (N)" and "Dev Test (M)" child nodes with the correct sample counts

---

### User Story 2 - Select Appropriate Data Selector for Learning Task (Priority: P1)

As a neural network experimenter, I want to choose different data selector types when creating training datasets, so that I can configure how input and target data are extracted from my samples for different learning scenarios (classification, autoencoding, super-resolution).

**Why this priority**: Different neural network learning tasks require different data mappings. Classification needs input-target pairs, autoencoders need input-input pairs, and super-resolution needs low-res to high-res mappings. This is essential for supporting diverse educational experiments and must work from the start.

**Independent Test**: Can be tested by creating training datasets with each selector type (ClassifierDataSelector, OnlyInputDataSelector, TargetAsInputDataSelector, SuperResolutionDataSelector) and verifying the selector appears correctly in the tree and persists when saved/loaded.

**Acceptance Scenarios**:

1. **Given** user is creating a training dataset for digit classification, **When** user selects "ClassifierDataSelector", **Then** the neural network will use sample.input as input and sample.target as expected output
2. **Given** user is creating a training dataset for autoencoder experiments, **When** user selects "OnlyInputDataSelector", **Then** the neural network will use sample.input as both input and target (for learning identity mapping)
3. **Given** user is creating a training dataset for super-resolution, **When** user selects "SuperResolutionDataSelector", **Then** the neural network will use sample.input (low-res) as input and sample.original (high-res) as target
4. **Given** user is experimenting with reverse mapping, **When** user selects "TargetAsInputDataSelector", **Then** the neural network will use sample.target as input and sample.input as output
5. **Given** a training dataset has been created with a data selector, **When** user views the dataset in the tree, **Then** a "DataSelector" child node displays the selected selector type
6. **Given** user edits a data selector node in the tree, **When** user changes the selector type via the combo box editor, **Then** the training dataset is updated with the new selector

---

### User Story 3 - Persist and Load Training Datasets (Priority: P2)

As a student conducting long-term experiments, I want to save and load my training datasets to/from JSON files, so that I can preserve my exact train/test splits and reuse them across multiple training sessions for reproducible experiments.

**Why this priority**: Reproducibility is crucial for scientific learning. Students need to compare different network architectures on identical data splits. This enables consistent experimentation but is secondary to the ability to create datasets in the first place.

**Independent Test**: Can be tested by creating a training dataset, saving it to a .json file, closing the application, restarting, loading the saved file, and verifying all properties (name, sample names, split percentages, data selector) are identical.

**Acceptance Scenarios**:

1. **Given** a training dataset "MNIST Experiment 1" exists with specific sample names and data selector, **When** user selects File > Save Training Data and chooses a file path, **Then** the dataset is serialized to JSON format with all properties preserved
2. **Given** a saved training dataset JSON file exists, **When** user selects File > Load Training Data and opens the file, **Then** the dataset is deserialized and added to the repository with all original properties
3. **Given** a loaded training dataset references sample names that exist in the repository, **When** the dataset is used for training, **Then** the correct samples are retrieved by name from the samples map
4. **Given** a loaded training dataset references sample names that no longer exist in the repository, **When** training is attempted, **Then** appropriate error handling occurs (samples cannot be retrieved)
5. **Given** training dataset contains a data selector configuration, **When** the JSON is saved, **Then** the data selector type is preserved using Jackson's @JsonTypeInfo annotation for polymorphic deserialization

---

### User Story 4 - View Training Dataset Composition (Priority: P2)

As a student learning about data preparation, I want to view the detailed composition of my training datasets in a tree structure, so that I can understand exactly which samples are in the training set versus the test set and verify my data organization.

**Why this priority**: Transparency in data organization is important for educational understanding. Students should be able to inspect and verify their data splits. However, this is primarily a viewing concern - the datasets must exist first (P1) before they can be viewed.

**Independent Test**: Can be tested by creating a training dataset, expanding all nodes in the tree, and verifying the display shows: dataset name with percentages, training samples list, test samples list, data selector type, and individual sample names in each list.

**Acceptance Scenarios**:

1. **Given** a training dataset contains 70 training samples and 30 test samples, **When** user views the dataset node, **Then** the label displays "DatasetName (100: 70% | 30%)" showing total, training %, and test %
2. **Given** a training dataset is expanded in the tree, **When** viewing the children, **Then** three child nodes appear: "Training (70)", "Dev Test (30)", and "DataSelector"
3. **Given** user expands the "Training (70)" node, **When** viewing the list, **Then** all 70 training sample names are displayed as individual leaf nodes in sorted order
4. **Given** user expands the "Dev Test (30)" node, **When** viewing the list, **Then** all 30 test sample names are displayed as individual leaf nodes in sorted order
5. **Given** training dataset tree nodes are displayed, **When** user clicks on individual sample name nodes, **Then** the sample preview is shown in the details panel (if sample still exists in repository)
6. **Given** training and test sample lists are displayed, **When** samples were partitioned, **Then** both lists are sorted alphabetically for easy scanning (per ShuffleService implementation)

---

### User Story 5 - Manage Training Dataset Lifecycle (Priority: P3)

As a student conducting multiple experiments, I want to rename and delete training datasets, so that I can keep my workspace organized and remove datasets that are no longer needed for my current learning objectives.

**Why this priority**: Lifecycle management improves the user experience but is not essential for core functionality. Users can work effectively even without rename/delete capabilities, making this a lower priority enhancement.

**Independent Test**: Can be tested by creating multiple training datasets, renaming one via the tree editor, deleting another via the remove button or delete key, and verifying the repository and tree UI update correctly.

**Acceptance Scenarios**:

1. **Given** a training dataset is selected in the tree, **When** user clicks "Edit" button or presses F2, **Then** the dataset name becomes editable in a text field
2. **Given** the dataset name editor is active, **When** user enters a new name and presses Enter, **Then** the TrainingData object's name is updated and the tree display refreshes
3. **Given** a training dataset is selected in the tree, **When** user clicks "Remove" button or presses Delete key, **Then** the dataset is removed from the repository and disappears from the tree
4. **Given** the TrainingDataListNode is selected, **When** user clicks "Remove" button, **Then** all training datasets are removed from the repository after confirmation
5. **Given** multiple training datasets exist, **When** user deletes one, **Then** other datasets remain unaffected and continue to function normally
6. **Given** a training dataset is in use by a training session, **When** user attempts to delete it, **Then** the deletion succeeds but the training session maintains its reference to the TrainingData object in memory

---

### User Story 6 - Understand Sample Shuffling and Deterministic Randomization (Priority: P3)

As an advanced student learning about reproducible machine learning, I want to understand how sample shuffling works with configurable random seeds, so that I can create reproducible experiments while still having random data partitioning.

**Why this priority**: This is an advanced educational concept. While the shuffling happens automatically (P1 functionality), understanding and controlling the random seed is valuable but not essential for basic neural network learning.

**Independent Test**: Can be tested by examining the ShuffleService configuration, verifying it uses the injected Random instance from RandomConfigurer.YAPRNN_RANDOM_BEAN, and confirming that multiple training dataset creations with the same seed produce identical sample orderings.

**Acceptance Scenarios**:

1. **Given** the application uses a configured Random instance, **When** user creates a training dataset, **Then** the ShuffleService uses this Random for reproducible shuffling
2. **Given** samples are being partitioned, **When** ShuffleService.partition() is called, **Then** samples are shuffled before splitting into training/test sets
3. **Given** the partition process completes, **When** training and test lists are created, **Then** both lists are sorted alphabetically after partitioning for consistent display
4. **Given** training percentage is 0.7 and test percentage is 0.3, **When** 100 samples are partitioned, **Then** normalization ensures exactly 70 training and 30 test samples (no rounding errors leave samples unused)
5. **Given** training percentage is 0.8 and test percentage is 0.4 (sum > 1.0), **When** samples are partitioned, **Then** normalization factor is 1.2, resulting in 66.67% training and 33.33% test distribution

---

### Edge Cases

- What happens when user creates a training dataset with 0% training or 0% test split? The system should allow this for experimentation purposes (e.g., testing only on all data).
- How does the system handle very small sample sets (e.g., 5 samples with 80/20 split)? The integer rounding in partition() should handle this gracefully, resulting in 4 training and 1 test sample.
- What happens when a loaded training dataset references sample names that no longer exist in the repository? The querySamplesByName() method will return null for missing samples, which should be handled by the training code.
- How does the system handle duplicate sample names in the repository? The samplesGroupedByName map will only keep the last sample with that name, potentially causing inconsistencies.
- What happens when user tries to save a training dataset without selecting a file location? The DialogsService should handle file chooser cancellation gracefully.
- How does the system handle training datasets with very long names? The UI tree should truncate or wrap long names appropriately for display.
- What happens when persistence fails due to file system errors (permissions, disk full)? The PersistenceService uses @SneakyThrows, so exceptions will propagate to the DialogsService for error display.
- How does the data selector polymorphic serialization work with custom or unknown selector types? Jackson's @JsonTypeInfo with use=Id.CLASS stores the fully qualified class name, enabling proper deserialization.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users to create training datasets from loaded samples via the "Randomized Training Data" button
- **FR-002**: System MUST partition samples into training and test sets based on user-specified percentage splits (e.g., 70% training, 30% test)
- **FR-003**: System MUST shuffle samples randomly before partitioning to ensure unbiased data distribution
- **FR-004**: System MUST normalize split percentages when their sum exceeds 100% to ensure all samples are allocated correctly
- **FR-005**: System MUST support four data selector types: ClassifierDataSelector, OnlyInputDataSelector, TargetAsInputDataSelector, SuperResolutionDataSelector
- **FR-006**: System MUST allow users to assign custom names to training datasets with auto-incrementing default names ("Randomized Training Data 1", "Randomized Training Data 2", etc.)
- **FR-007**: System MUST validate that dataset names are non-blank before allowing creation
- **FR-008**: System MUST persist training datasets to JSON files including name, training sample names, test sample names, and data selector configuration
- **FR-009**: System MUST load training datasets from JSON files and restore all properties including polymorphic data selector types
- **FR-010**: System MUST display training datasets in a tree structure showing: dataset name with statistics, training samples list, test samples list, and data selector
- **FR-011**: System MUST display dataset statistics in the format "Name (total: training% | test%)" in the tree node label
- **FR-012**: System MUST sort training and test sample name lists alphabetically after partitioning for consistent display
- **FR-013**: System MUST store sample names (strings) rather than sample object references in TrainingData to support serialization
- **FR-014**: System MUST resolve sample names to actual Sample objects via Repository.querySamplesByName() when training begins
- **FR-015**: System MUST allow users to edit training dataset names inline in the tree view
- **FR-016**: System MUST allow users to delete training datasets via Remove button or Delete key
- **FR-017**: System MUST allow users to edit data selector types via inline combo box editor in the tree view
- **FR-018**: System MUST use a configured Random instance (YAPRNN_RANDOM_BEAN) for reproducible shuffling
- **FR-019**: System MUST fire repository change events (OnRepositoryElementsChanged, OnRepositoryElementsRemoved) when training datasets are added or removed
- **FR-020**: System MUST update all dependent UI components (TrainingFrame) when training datasets are added, removed, or selected

### Key Entities

- **TrainingData**: Represents a named collection of training and test samples with an associated data selector
  - name: String - User-assigned name for the dataset
  - trainingSampleNames: List<String> - Names of samples allocated to training set
  - devTestSampleNames: List<String> - Names of samples allocated to development/test set
  - dataSelector: DataSelector - Strategy for extracting input/target pairs from samples
  - toString(): Returns formatted string "name (trainingCount|testCount)"

- **DataSelector**: Sealed interface defining strategies for mapping samples to input/target pairs for different learning tasks
  - input(Sample): Extracts input vector from a sample
  - target(Sample, ActivationFunction): Extracts target vector from a sample with activation function applied
  - postprocessOutput(float[], float[], ActivationFunction): Post-processes network output
  - getOutputWidth(ImageSample): Returns expected output width for image samples
  - Implementations: ClassifierDataSelector, OnlyInputDataSelector, TargetAsInputDataSelector, SuperResolutionDataSelector

- **Sample**: Sealed interface representing a data sample (ImageSample, SimpleSample, SoundSample)
  - name: String - Unique identifier for the sample
  - input: float[] - Input vector representation
  - target: float[] - Target/label vector representation
  - original: float[] - Original high-resolution data (for super-resolution)
  - label: String - Human-readable label
  - file: File - Source file location

- **Repository**: Singleton holding all application data including samples and training datasets
  - samples: List<Sample> - All loaded samples
  - samplesGroupedByName: Map<String, Sample> - Lookup map for samples by name
  - trainingDataList: List<TrainingData> - All created training datasets
  - querySamplesByName(List<String>): Resolves sample names to Sample objects

- **ShuffleService**: Service for randomizing and partitioning sample collections
  - partition(samples, trainingPercentage, devTestPercentage, mapper): Shuffles samples and splits into training/test sets with percentage normalization
  - shuffleList(items): Randomly shuffles a list using the configured Random instance

- **TrainingDataNode**: Tree model node representing a TrainingData entity in the UI
  - Children: SampleNameListNode (Training), SampleNameListNode (Dev Test), DataSelectorNode
  - Label format: "name (total: training% | test%)"

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a training dataset with train/test split in under 30 seconds from loaded samples
- **SC-002**: Training datasets accurately reflect specified split percentages within 1% margin (due to integer rounding)
- **SC-003**: Saved training datasets can be loaded and reproduce exact sample membership in training/test sets
- **SC-004**: Data selector type is correctly preserved through save/load cycles for all four selector types
- **SC-005**: Users can view complete training dataset composition (all sample names in both sets) through tree expansion
- **SC-006**: Training dataset creation with 1000 samples completes in under 2 seconds
- **SC-007**: Sample shuffling with the same random seed produces identical train/test splits on repeated dataset creation
- **SC-008**: 100% of training dataset operations (create, save, load, rename, delete) work correctly with all four data selector types
- **SC-009**: Training frame successfully retrieves samples by name from training datasets with 0% lookup failure rate for valid sample names
- **SC-010**: Tree UI updates reflect training dataset changes (add/remove/rename) within 100ms of the operation

## Technical Context *(optional)*

### Architecture Notes

- **Dependency Injection**: Uses Jakarta CDI (javax.inject) for dependency management with @Singleton and @Inject annotations
- **Data Mapping**: MapStruct (TrainingDataMapper) generates mapping code from Parameters to TrainingData
- **Serialization**: Jackson ObjectMapper with @JsonTypeInfo for polymorphic data selector serialization
- **Event System**: Custom event router pattern (OnRepositoryElementsChanged, OnRepositoryElementsRemoved, OnTrainingDataSelected) for decoupled component communication
- **Tree Model**: Custom Swing TreeModel implementation (NetworksTreeModel) with ModelNode hierarchy for UI representation
- **Random Configuration**: Named bean (YAPRNN_RANDOM_BEAN) provides configured Random instance for reproducible shuffling

### Data Flow

1. User clicks "Randomized Training Data" button → RandomizeTrainingDataPanel dialog shown
2. User configures name, percentages, data selector → Parameters object created
3. TrainingDataMapper.from(Parameters, List<Sample>) called
4. ShuffleService.partition() shuffles samples and splits by percentages
5. TrainingData object created with sample names and data selector
6. Repository.addTrainingData() stores dataset and fires OnRepositoryElementsChanged event
7. NetworksTreeModel.refreshTrainingDataList() updates tree UI
8. TrainingFrame receives event and updates training data selection controls
9. On save: PersistenceService.saveTrainingData() serializes to JSON with Jackson
10. On load: PersistenceService.loadTrainingData() deserializes from JSON
11. At training time: Repository.querySamplesByName() resolves sample names to Sample objects
12. DataSelector.input() and target() extract input/target vectors for network training

### File Structure

```
src/main/java/edu/yaprnn/
├── training/
│   ├── TrainingData.java                    # Core entity
│   ├── ShuffleService.java                  # Partitioning logic
│   └── selectors/
│       ├── DataSelector.java                # Sealed interface
│       ├── ClassifierDataSelector.java      # For classification tasks
│       ├── OnlyInputDataSelector.java       # For autoencoders
│       ├── TargetAsInputDataSelector.java   # For reverse mapping
│       └── SuperResolutionDataSelector.java # For super-resolution
├── model/
│   └── Repository.java                      # Data storage
├── gui/
│   ├── views/
│   │   ├── MainFrame.java                   # Main UI with create/save/load actions
│   │   ├── RandomizeTrainingDataPanel.java  # Creation dialog
│   │   └── TrainingFrame.java               # Training UI
│   ├── model/
│   │   ├── NetworksTreeModel.java           # Tree model coordination
│   │   └── nodes/
│   │       ├── TrainingDataNode.java        # Tree node representation
│   │       ├── TrainingDataListNode.java    # List container node
│   │       ├── DataSelectorNode.java        # Selector node
│   │       └── SampleNameListNode.java      # Sample list nodes
│   ├── services/
│   │   └── PersistenceService.java          # JSON serialization
│   └── views/mappings/
│       └── TrainingDataMapper.java          # MapStruct mapper
```

### Dependencies

- **Lombok**: @Getter, @Setter, @RequiredArgsConstructor, @AllArgsConstructor annotations
- **Jackson**: ObjectMapper for JSON serialization with @JsonTypeInfo for polymorphic types
- **MapStruct**: Abstract mapper class with CDI component model
- **Jakarta CDI**: @Inject, @Singleton, @Named for dependency injection
- **Swing**: JTree, TreeModel, JPanel, JTextField, JSpinner, JComboBox for UI

## Open Questions *(optional)*

1. Should the system prevent deletion of training datasets that are currently in use by active training sessions?
2. Should there be a validation warning when loaded training datasets reference samples that no longer exist in the repository?
3. Should the system support importing/exporting multiple training datasets in batch operations?
4. Should training datasets maintain version information for tracking changes over time?
5. Should the data selector be editable after dataset creation, or should it be immutable to preserve experiment integrity?
6. Should the system support custom data selectors defined by users for advanced experimentation?
7. Should there be a visualization showing the distribution of labels/classes in training vs test sets?
8. Should the system track and display which training datasets were used to train which networks for experiment tracking?
9. Should there be an "unsaved changes" indicator when training datasets are modified but not saved?
10. Should the system support stratified sampling to ensure balanced class distribution in training/test splits?
