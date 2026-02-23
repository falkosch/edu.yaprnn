# Feature Specification: Classification and Inference

**Feature Branch**: `classification-inference`
**Created**: 2026-02-23
**Status**: Draft
**Input**: User description: "Analyze the classification/inference functionality in the edu.yaprnn project"

## User Scenarios & Testing (mandatory)

### User Story 1 - Single Sample Classification (Priority: P1)

A student wants to classify a single handwritten digit using a trained neural network to see how the network interprets that specific input and understand the network's confidence in its prediction.

**Why this priority**: This is the core functionality of inference - classifying individual samples is the most fundamental use case and demonstrates the network's learned capabilities. Without this, the application cannot demonstrate what the network has learned.

**Independent Test**: Can be fully tested by loading a trained network, selecting a sample from the dataset, clicking "Classify", and verifying that the output shows layer-by-layer activations and a predicted class label.

**Acceptance Scenarios**:

1. **Given** a trained network is loaded and a sample is selected, **When** the user clicks the "Classify" button, **Then** the system performs feed-forward computation through all layers and displays the output layer activations
2. **Given** classification is complete, **When** viewing the results table, **Then** each layer's pre-activation values (v) and post-activation values (h) are displayed for all neurons
3. **Given** a classification result with output layer activations, **When** the system determines the predicted class, **Then** it selects the neuron with the maximum activation value as the predicted class
4. **Given** a sample with known labels, **When** classification completes, **Then** the interface displays both the predicted output and the ground truth label for comparison
5. **Given** an image-based sample, **When** classification completes, **Then** the output reconstruction view displays a visual representation of the network's output interpreted as an image

---

### User Story 2 - Batch Accuracy Evaluation (Priority: P2)

A student wants to evaluate their network's overall performance on a test set to understand how well it generalizes beyond individual samples and track training progress.

**Why this priority**: Understanding overall accuracy is essential for evaluating network performance and making training decisions. This enables students to objectively measure their network's quality.

**Independent Test**: Can be tested by loading a trained network and a test dataset, triggering accuracy computation, and verifying that both error rate and hit rate (accuracy) metrics are calculated and displayed.

**Acceptance Scenarios**:

1. **Given** a network and a collection of test samples, **When** computing accuracy, **Then** the system classifies all samples in parallel and aggregates the results
2. **Given** classification results for all samples, **When** computing accuracy metrics, **Then** the system calculates the average loss function error across all samples
3. **Given** classification results with predicted and target outputs, **When** determining hits, **Then** a sample is counted as a hit if the argmax of predicted output matches the argmax of target output
4. **Given** aggregated results, **When** accuracy computation completes, **Then** the system returns both the hit rate (percentage correct) and average error value
5. **Given** training is in progress, **When** each epoch completes, **Then** accuracy is computed for both training and dev-test sets and plotted on the training progress graph

---

### User Story 3 - Layer-by-Layer Activation Inspection (Priority: P2)

A student wants to inspect the intermediate layer activations during classification to understand how information flows through the network and how features are progressively transformed.

**Why this priority**: This educational transparency helps students understand the internal workings of neural networks, showing how raw input is transformed into high-level representations that enable classification.

**Independent Test**: Can be tested by classifying a sample and verifying that the results table displays both v (weighted sum) and h (activated output) values for each layer, with all neuron values visible.

**Acceptance Scenarios**:

1. **Given** a classification has been performed, **When** viewing the layers table, **Then** all network layers from input to output are displayed with their respective neuron values
2. **Given** a layer in the results table, **When** examining neuron values, **Then** both v[i] (pre-activation: weighted sum plus bias) and h[i] (post-activation: after activation function) are shown
3. **Given** multiple layers in a network, **When** viewing the classification table, **Then** columns are organized as: Index, Layer0-v, Layer0-h, Layer1-v, Layer1-h, ..., Output, Label
4. **Given** layers with different sizes, **When** displaying the table, **Then** rows extend to accommodate the largest layer, with empty cells for smaller layers
5. **Given** floating-point activation values, **When** displayed in the table, **Then** values are formatted to 4 decimal places with proper sign handling

---

### User Story 4 - Output Reconstruction Visualization (Priority: P3)

A student working with autoencoders or image generation networks wants to visualize the network's output as an image to understand what the network is reconstructing or generating.

**Why this priority**: For certain network architectures (autoencoders, generative models, super-resolution), visualizing the output as an image is essential for understanding what the network has learned. This is architecture-specific and not needed for simple classifiers.

**Independent Test**: Can be tested by classifying an image sample with a network that has image-sized output, and verifying that the output reconstruction tab displays a visual rendering of the output values as grayscale pixels.

**Acceptance Scenarios**:

1. **Given** a classification result with image sample, **When** the output has spatial dimensions, **Then** the output reconstruction panel displays the output as a grayscale image
2. **Given** output reconstruction is displayed, **When** adjusting the gamma slider, **Then** the image brightness is adjusted using gamma correction (pixel^gamma transformation)
3. **Given** output reconstruction is displayed, **When** adjusting the zoom control, **Then** the image is scaled using bicubic interpolation for smooth visualization
4. **Given** a non-image sample or incompatible output size, **When** viewing the output reconstruction tab, **Then** no image is displayed (empty panel)
5. **Given** an output with values outside [0,1] range, **When** rendering the reconstruction, **Then** pixel values are clamped to the valid 0-255 range after gamma correction

---

### User Story 5 - Classification on Untrained Networks (Priority: P3)

A student wants to classify samples using a newly initialized, untrained network to observe random behavior and contrast it with trained network performance.

**Why this priority**: Comparing untrained vs trained networks helps students understand the value of learning. This educational comparison demonstrates what networks know "innately" (nothing) versus what they learn from data.

**Independent Test**: Can be tested by initializing a new network without training, classifying samples, and observing that outputs are near-random and accuracy is close to chance level.

**Acceptance Scenarios**:

1. **Given** a newly initialized network with random weights, **When** classifying a sample, **Then** the system successfully performs feed-forward computation with the random weights
2. **Given** an untrained network, **When** computing accuracy on a dataset, **Then** the hit rate should be approximately 1/num_classes (chance level for classification)
3. **Given** untrained network outputs, **When** examining layer activations, **Then** values appear random with no meaningful patterns related to input features
4. **Given** an untrained vs trained network, **When** comparing outputs on the same sample, **Then** students can observe how training moves the network from random to meaningful predictions
5. **Given** an untrained network, **When** the error metric is computed, **Then** it should be significantly higher than a trained network's error

---

### Edge Cases

- What happens when classifying with a network whose architecture doesn't match the sample dimensions?
  - The system handles this by copying or truncating input arrays to match expected layer sizes (see `Arrays.copyOf` in feedForward)

- How does the system handle samples with missing labels?
  - Labels are optional for display; classification proceeds using the sample's target values, with empty strings displayed for missing label indices

- What happens when all output activations are identical?
  - The argmax function deterministically selects the first index with the maximum value, ensuring consistent behavior

- How does parallel classification handle thread safety?
  - Each classification task creates independent layer arrays and operates on immutable network weights, ensuring thread-safe parallel execution

- What happens when computing accuracy on an empty sample set?
  - The system throws an exception from `orElseThrow()` in the reduce operation, indicating no samples were provided

- How are NaN or Infinity values handled in activations?
  - These propagate through the network computation; activation functions and loss functions should handle edge cases, but extreme values can occur with poorly configured networks

## Requirements (mandatory)

### Functional Requirements

- **FR-001**: System MUST perform feed-forward computation through all network layers for a given input sample
- **FR-002**: System MUST compute pre-activation values (v) using matrix multiplication of inputs and weights plus bias terms
- **FR-003**: System MUST compute post-activation values (h) by applying the layer's activation function to pre-activation values
- **FR-004**: System MUST determine predicted class by finding the index of the maximum value in the output layer activations
- **FR-005**: System MUST compare predicted output with target output using argmax comparison for accuracy calculation
- **FR-006**: System MUST compute accuracy metrics including hit rate and average error across sample collections
- **FR-007**: System MUST execute batch classification in parallel using configurable thread pools
- **FR-008**: System MUST display all layer activations in a tabular format with index, v/h columns, output, and labels
- **FR-009**: System MUST reconstruct output as a visual image when output dimensions match image sample dimensions
- **FR-010**: System MUST enable classification on both trained and untrained networks
- **FR-011**: System MUST apply appropriate data selector transformations to inputs and targets before classification
- **FR-012**: System MUST maintain immutability of network weights during read-only classification operations
- **FR-013**: Users MUST be able to select a sample and network from dropdown controls to configure classification
- **FR-014**: Users MUST be able to view classification results across multiple tabs (sample details, layers, output reconstruction)
- **FR-015**: System MUST apply zoom and gamma adjustments to output reconstruction visualization

### Key Entities

- **Layer**: Represents a single layer's state during feed-forward computation, containing layer index, pre-activation values (v), post-activation values (h), and activation function reference
- **AccuracyResult**: Aggregates classification performance metrics including total error, sample count, and hit count, supporting reduction operations for batch evaluation
- **DataSelector**: Defines transformations for extracting network inputs from samples and converting expected outputs to appropriate target formats for different network architectures
- **MultiLayerNetwork**: Contains network architecture (layer sizes, activation functions, weights) and provides methods for feed-forward computation and accuracy evaluation

## Success Criteria (mandatory)

### Measurable Outcomes

- **SC-001**: Students can classify a single sample and view results in under 1 second for networks with up to 1000 neurons
- **SC-002**: System accurately computes batch accuracy for 10,000 samples in under 5 seconds using parallel processing
- **SC-003**: Classification results display all layer activations with 4 decimal places precision
- **SC-004**: Hit rate calculation correctly identifies when argmax(predicted) equals argmax(target) for multi-class classification
- **SC-005**: Untrained networks demonstrate near-chance accuracy (within 5% of 1/num_classes) on balanced datasets
- **SC-006**: Trained digit classification networks achieve >90% accuracy on MNIST test set after proper training
- **SC-007**: Output reconstruction displays images at configurable zoom levels (1x to 10x) with smooth interpolation
- **SC-008**: Layer activation table displays values for networks with up to 10 layers without UI degradation
- **SC-009**: Parallel classification utilizes all available CPU cores (configurable up to system parallelism limit)
- **SC-010**: Classification on the same sample produces identical results across multiple executions (deterministic behavior)
