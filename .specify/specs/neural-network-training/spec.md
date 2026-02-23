# Feature Specification: Neural Network Training System

**Feature Branch**: `[neural-network-training]`
**Created**: 2026-02-23
**Status**: Draft
**Input**: User description: "Document the neural network training functionality in the edu.yaprnn educational application"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configure and Execute Basic Training (Priority: P1)

A student or educator wants to train a neural network on a dataset (such as MNIST digits) using basic training parameters to observe how the network learns over time.

**Why this priority**: This is the core functionality that delivers immediate educational value. Users can start experimenting with neural network training and see results without understanding advanced concepts.

**Independent Test**: Can be fully tested by configuring a network with default parameters, selecting a dataset, starting training, and observing the training progress graph showing decreasing error and increasing accuracy. Delivers immediate value of seeing a neural network learn.

**Acceptance Scenarios**:

1. **Given** a trained dataset is available and a network architecture is configured, **When** the user selects the dataset and network from dropdown menus and clicks "Start", **Then** the training process begins and the progress graph updates showing training error and accuracy metrics over epochs
2. **Given** training is in progress, **When** the user observes the training progress graph, **Then** the graph displays four curves: training error, training accuracy, dev test error, and dev test accuracy, all updating in real-time as epochs complete
3. **Given** training is in progress, **When** training error falls below the configured maximum error threshold OR maximum iterations are reached, **Then** training automatically stops and the final network weights are preserved
4. **Given** training is in progress, **When** the user clicks "Stop", **Then** training halts immediately and partial training results are retained in the network

---

### User Story 2 - Select Training Mode and Configure Batch Learning (Priority: P1)

A student wants to experiment with different training approaches by comparing online learning (updating weights after each sample) versus batch learning (accumulating gradients across multiple samples) to understand their trade-offs.

**Why this priority**: Understanding different training modes is fundamental to neural network education. This enables users to experiment with a core concept that directly affects training behavior and convergence patterns.

**Independent Test**: Can be fully tested by switching between "ONLINE" and "BATCH" training methods, configuring batch size (for batch mode), and observing different convergence patterns and training speeds. Delivers educational value about gradient descent variants.

**Acceptance Scenarios**:

1. **Given** the training configuration panel is displayed, **When** the user selects "ONLINE" training method, **Then** the network updates weights immediately after processing each training sample
2. **Given** the training configuration panel is displayed, **When** the user selects "BATCH" training method and sets a batch size, **Then** the network accumulates gradients across the specified batch size before updating weights
3. **Given** batch training is selected, **When** the user adjusts the batch size parameter (default: 100), **Then** training uses mini-batches of the specified size to balance convergence speed and stability
4. **Given** a dataset with multiple samples, **When** each training epoch executes, **Then** samples are shuffled randomly to prevent learning bias from sample ordering

---

### User Story 3 - Configure Learning Rate Strategies (Priority: P2)

An advanced student wants to control how the learning rate changes during training by choosing between constant, periodic decay, or adaptive adjustment strategies to optimize training convergence.

**Why this priority**: Learning rate tuning is critical for effective training but requires understanding basic training first. This enables experimentation with an intermediate-level concept that significantly impacts training outcomes.

**Independent Test**: Can be fully tested by selecting each learning rate modifier (CONSTANT, PERIODIC, ADAPTIVE), configuring their parameters, and observing how learning rate changes affect training convergence and speed. Delivers educational value about hyperparameter optimization.

**Acceptance Scenarios**:

1. **Given** the training configuration panel displays learning rate options, **When** the user selects "CONSTANT" modifier, **Then** the learning rate remains fixed at the configured value (default: 0.02) throughout all training epochs
2. **Given** the user selects "PERIODIC" modifier, **When** the user configures a change interval (default: 5 epochs) and descend factor (default: 0.98), **Then** the learning rate multiplies by the descend factor every N epochs
3. **Given** the user selects "ADAPTIVE" modifier, **When** the user configures ascend (default: 1.01) and descend (default: 0.98) factors, **Then** the learning rate increases when training error decreases and decreases when training error increases
4. **Given** any learning rate strategy is active, **When** training progresses, **Then** the system logs the current learning rate value for each epoch allowing users to track adjustments

---

### User Story 4 - Configure Regularization Parameters (Priority: P2)

A student studying overfitting wants to apply L1 and L2 regularization to prevent the network from memorizing training data and improve generalization to test data.

**Why this priority**: Regularization is an important concept for understanding model generalization but requires understanding basic training behavior first. This enables experimentation with techniques that improve real-world model performance.

**Independent Test**: Can be fully tested by training with and without L1/L2 regularization, adjusting decay parameters, and comparing training vs. dev test accuracy to observe overfitting reduction. Delivers educational value about bias-variance tradeoff.

**Acceptance Scenarios**:

1. **Given** the training configuration panel displays regularization options, **When** the user sets L1 decay parameter (default: 0.001), **Then** weight updates include L1 penalty based on the sign of each weight
2. **Given** the user sets L2 decay parameter (default: 0.001), **When** training executes, **Then** weight updates include L2 penalty proportional to weight magnitude
3. **Given** both L1 and L2 decay are configured, **When** gradient descent updates weights, **Then** both penalties are applied additively to constrain weight growth
4. **Given** regularization is enabled, **When** observing the training progress graph, **Then** users can see reduced gap between training and dev test accuracy indicating better generalization

---

### User Story 5 - Configure Momentum for Faster Convergence (Priority: P3)

An advanced student wants to use momentum to accelerate training and help the optimizer navigate through saddle points and local minima more effectively.

**Why this priority**: Momentum is an advanced optimization technique that builds on understanding of gradient descent. It's valuable for speeding up training but not essential for understanding core concepts.

**Independent Test**: Can be fully tested by adjusting momentum parameter (default: 0.2), training with and without momentum, and comparing convergence speed and stability. Delivers educational value about optimization dynamics.

**Acceptance Scenarios**:

1. **Given** the training configuration panel displays momentum parameter, **When** the user sets momentum value between -1.0 and 1.0 (default: 0.2), **Then** weight updates incorporate velocity from previous gradient updates
2. **Given** momentum is configured with a positive value, **When** consecutive gradients point in similar directions, **Then** weight updates accelerate in that direction improving convergence speed
3. **Given** momentum is zero, **When** training executes, **Then** weight updates use only current gradient without historical velocity

---

### User Story 6 - Configure Parallelization for Training Performance (Priority: P3)

A user with large datasets wants to control the degree of parallelism during training to leverage multi-core processors and reduce training time.

**Why this priority**: Performance optimization is important for practical usage but doesn't affect the educational value of understanding training concepts. Users can start with default parallelism settings.

**Independent Test**: Can be fully tested by adjusting max parallelism setting, monitoring training iteration time logs, and observing reduced training time with higher parallelism on multi-core systems. Delivers practical value for working with larger datasets.

**Acceptance Scenarios**:

1. **Given** the training configuration panel displays max parallelism option, **When** the user sets parallelism value (default: system core count), **Then** gradient computations utilize up to that many parallel threads
2. **Given** online training mode is selected, **When** parallelization is enabled, **Then** multiple samples are processed concurrently but weight updates are applied sequentially to maintain training consistency
3. **Given** batch training mode is selected, **When** parallelization is enabled, **Then** gradient computations for samples within a batch execute in parallel and accumulated gradients are applied once per batch
4. **Given** training is executing, **When** each epoch completes, **Then** the system logs iteration time allowing users to assess performance impact of parallelization settings

---

### User Story 7 - Monitor and Analyze Training Progress (Priority: P2)

A user wants to monitor training progress through visual feedback showing how error decreases and accuracy increases over time, and compare training vs. test set performance to detect overfitting.

**Why this priority**: Visual feedback is essential for understanding training dynamics and is tightly coupled with the training process. However, training can technically function without visualization, making this secondary to the core training execution.

**Independent Test**: Can be fully tested by starting training, observing the real-time graph that plots four metrics over epochs, and reading logged output showing detailed metrics per epoch. Delivers immediate feedback about training effectiveness.

**Acceptance Scenarios**:

1. **Given** training is in progress, **When** each epoch completes, **Then** the system plots four data points on the progress graph: training error, training accuracy, dev test error, dev test accuracy
2. **Given** the training progress graph is displayed, **When** users observe the curves, **Then** the graph clearly distinguishes between training set metrics and dev test set metrics allowing comparison
3. **Given** training is in progress, **When** each epoch completes, **Then** the system logs detailed metrics including: epoch number, learning rate, training accuracy, training error, test accuracy, test error, and iteration time
4. **Given** training has completed multiple epochs, **When** the user clicks "Clear graph", **Then** all plotted data points are removed allowing a fresh training run to be visualized
5. **Given** training accuracy is calculated, **When** network output is compared to target, **Then** accuracy counts samples where the maximum output activation occurs at the same index as the maximum target value (correct classification)

---

### Edge Cases

- What happens when training data or network selection becomes invalid during training (e.g., network is deleted from repository while training is active)?
- How does the system handle training iterations where error increases significantly (numerical instability or divergence)?
- What happens when batch size exceeds the total number of training samples?
- How does the system behave when learning rate becomes extremely small (approaching zero) or extremely large (causing overflow)?
- What happens if the user closes the training window while training is in progress?
- How does the system handle very long training runs (thousands or millions of iterations)?
- What happens when max parallelism is set to 1 versus maximum available cores in terms of training behavior consistency?
- How does adaptive learning rate respond when training error oscillates (neither consistently increasing nor decreasing)?

## Requirements *(mandatory)*

### Functional Requirements

#### Training Execution
- **FR-001**: System MUST allow users to select a training dataset from available datasets in the repository
- **FR-002**: System MUST allow users to select a neural network from available networks in the repository
- **FR-003**: System MUST validate that both dataset and network are selected before allowing training to start
- **FR-004**: System MUST provide a "Start" button to initiate training when valid selections are made
- **FR-005**: System MUST provide a "Stop" button to halt training at any point during execution
- **FR-006**: System MUST disable the "Start" button during active training and enable the "Stop" button
- **FR-007**: System MUST re-enable the "Start" button and disable the "Stop" button when training completes or is stopped
- **FR-008**: System MUST preserve trained network weights when training completes or is stopped
- **FR-009**: System MUST notify other components when network weights are modified during training

#### Training Modes
- **FR-010**: System MUST support "ONLINE" training mode where weights update after each individual sample
- **FR-011**: System MUST support "BATCH" training mode where weights update after processing a configured batch of samples
- **FR-012**: Users MUST be able to select training mode via a dropdown control
- **FR-013**: System MUST provide a batch size parameter configurable from 1 to unlimited (default: 100)
- **FR-014**: System MUST shuffle training samples randomly at the start of each epoch
- **FR-015**: System MUST process samples in batches by dividing the training set into chunks of the configured batch size

#### Learning Rate Strategies
- **FR-016**: System MUST support "CONSTANT" learning rate strategy maintaining a fixed learning rate throughout training
- **FR-017**: System MUST support "PERIODIC" learning rate strategy that decreases learning rate at fixed epoch intervals
- **FR-018**: System MUST support "ADAPTIVE" learning rate strategy that adjusts based on training error trends
- **FR-019**: Users MUST be able to configure base learning rate value (default: 0.02, range: 0.0 to 1.0)
- **FR-020**: System MUST provide learning rate change interval parameter for periodic strategy (default: 5, range: 1 to unlimited)
- **FR-021**: System MUST provide learning rate ascend factor for adaptive strategy (default: 1.01, range: 0.0 to 2.0)
- **FR-022**: System MUST provide learning rate descend factor for periodic and adaptive strategies (default: 0.98, range: 0.0 to 1.0)
- **FR-023**: System MUST apply descend factor every N epochs in periodic mode
- **FR-024**: System MUST increase learning rate (multiply by ascend factor) when training error decreases in adaptive mode
- **FR-025**: System MUST decrease learning rate (multiply by descend factor) when training error increases in adaptive mode
- **FR-026**: System MUST keep learning rate unchanged when training error remains constant in adaptive mode

#### Training Parameters
- **FR-027**: System MUST provide maximum iterations parameter to limit training duration (default: 50, range: 1 to unlimited)
- **FR-028**: System MUST provide maximum training error threshold to stop when target accuracy is reached (default: 0.001, range: 0.001 to unlimited)
- **FR-029**: System MUST terminate training when maximum iterations are reached
- **FR-030**: System MUST terminate training when training error falls below maximum error threshold
- **FR-031**: System MUST provide momentum parameter (default: 0.2, range: -1.0 to 1.0)
- **FR-032**: System MUST incorporate previous gradient direction into weight updates when momentum is non-zero
- **FR-033**: System MUST provide L1 regularization decay parameter (default: 0.001, range: -1.0 to 1.0)
- **FR-034**: System MUST provide L2 regularization decay parameter (default: 0.001, range: -1.0 to 1.0)
- **FR-035**: System MUST apply L1 penalty proportional to the sign of each weight during gradient updates
- **FR-036**: System MUST apply L2 penalty proportional to weight magnitude during gradient updates
- **FR-037**: System MUST provide max parallelism parameter to control concurrent gradient computations (default: system core count, range: 1 to system core count)

#### Training Algorithm
- **FR-038**: System MUST compute gradients for each sample using backpropagation through all network layers
- **FR-039**: System MUST accumulate gradients across batch samples in batch training mode
- **FR-040**: System MUST apply accumulated gradients divided by batch size to calculate average gradient
- **FR-041**: System MUST update network weights using gradient descent with configured learning rate
- **FR-042**: System MUST reset previous gradient history when network weights are reset
- **FR-043**: System MUST use the network's configured activation functions for forward pass computations
- **FR-044**: System MUST use the network's configured loss function for error calculations

#### Progress Monitoring
- **FR-045**: System MUST compute accuracy metrics on both training set and dev test set after each epoch
- **FR-046**: System MUST calculate network error using the configured loss function
- **FR-047**: System MUST calculate hit rate (accuracy) by counting samples where predicted class matches target class
- **FR-048**: System MUST display a real-time graph plotting training error over epochs
- **FR-049**: System MUST display training accuracy (hit rate) on the same graph
- **FR-050**: System MUST display dev test error on the same graph for comparison
- **FR-051**: System MUST display dev test accuracy on the same graph for comparison
- **FR-052**: System MUST log detailed metrics for each epoch including: epoch number, learning rate, training accuracy, training error, test accuracy, test error
- **FR-053**: System MUST log iteration time for each epoch to allow performance monitoring
- **FR-054**: System MUST provide a "Clear graph" button to reset plotted training history
- **FR-055**: System MUST fix graph Y-axis range from 0.0 to 1.0 for comparing error and accuracy metrics

#### Parallelization
- **FR-056**: System MUST support parallel gradient computation using virtual threads
- **FR-057**: System MUST limit concurrent thread count to the configured max parallelism value
- **FR-058**: System MUST ensure thread-safe weight updates in online training mode despite parallel gradient computation
- **FR-059**: System MUST parallelize gradient computation across batch samples in batch training mode
- **FR-060**: System MUST reduce parallelism overhead by using thread pools rather than creating threads per sample

#### User Interface
- **FR-061**: System MUST provide a dedicated training window with toolbar controls and configuration panel
- **FR-062**: System MUST display training configuration panel with all adjustable parameters organized in groups
- **FR-063**: System MUST display training dataset selector as a dropdown in the toolbar
- **FR-064**: System MUST display network selector as a dropdown in the toolbar
- **FR-065**: System MUST update window title to show selected training dataset and network names
- **FR-066**: System MUST automatically stop training when the training window is closed
- **FR-067**: System MUST use spinner controls for numeric parameters with appropriate min/max/step values
- **FR-068**: System MUST use dropdown controls for enumerated options (training mode, learning rate modifier)

### Key Entities *(include if feature involves data)*

- **Training Dataset**: A collection of samples divided into training set and dev test set, with a data selector that extracts input and target values from samples. Used to provide examples for learning and validation.

- **Neural Network**: A multi-layer architecture with configured layer sizes, activation functions, bias value, loss function, and weight matrices. The entity being trained through gradient descent optimization.

- **Training Configuration**: The set of hyperparameters controlling training behavior including: max iterations, max error threshold, training mode, batch size, learning rate, learning rate strategy, momentum, regularization decay factors, and parallelism settings.

- **Learning Rate State**: The dynamic learning rate value and associated state (iteration count, previous error) that evolves during training according to the selected strategy (constant, periodic, or adaptive).

- **Training Progress**: The historical record of training metrics across epochs including training error, training accuracy, dev test error, dev test accuracy, and learning rate values. Used for visualization and analysis.

- **Gradient Matrices**: The computed error gradients for each layer's weights during backpropagation. In online mode, applied immediately; in batch mode, accumulated across samples before application.

- **Accuracy Result**: The computed metrics for a set of samples including total error (sum of individual sample losses), sample count, and hit count (number of correct classifications). Averaged to produce final accuracy percentage.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can configure and start training a neural network on a dataset within 30 seconds of opening the training window
- **SC-002**: Training progress graph updates in real-time as epochs complete, providing immediate visual feedback within 1 second of epoch completion
- **SC-003**: Users can switch between online and batch training modes and observe different convergence patterns demonstrating their educational understanding
- **SC-004**: Training automatically stops when configured thresholds are met (max iterations or target error) without user intervention
- **SC-005**: Users can experiment with at least three learning rate strategies (constant, periodic, adaptive) and observe their impact on training dynamics
- **SC-006**: Parallelization with max parallelism set to system core count reduces training time by at least 50% compared to single-threaded execution on multi-core systems
- **SC-007**: Training progress log provides detailed per-epoch metrics enabling users to analyze training behavior quantitatively
- **SC-008**: Users can apply L1/L2 regularization and observe reduced overfitting (smaller gap between training and test accuracy) compared to unregularized training
- **SC-009**: The training system successfully handles datasets ranging from dozens to tens of thousands of samples without crashes or performance degradation
- **SC-010**: Users can stop and restart training multiple times without data corruption or system instability
- **SC-011**: The training interface remains responsive during training allowing users to monitor progress and stop training at any time
- **SC-012**: Training with adaptive learning rate converges faster (fewer epochs to reach target error) than constant learning rate for typical datasets, demonstrating the strategy's effectiveness
