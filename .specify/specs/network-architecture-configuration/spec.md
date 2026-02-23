# Feature Specification: Network Architecture Configuration

**Feature Branch**: `[network-architecture-configuration]`
**Created**: 2026-02-23
**Status**: Draft
**Input**: User description: "Analyze the network architecture configuration functionality in the edu.yaprnn project and write a speckit-conforming specification."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Basic Network Template (Priority: P1)

A user wants to create a network template by specifying the number of layers, layer sizes, activation functions, loss function, and bias values. This template serves as a reusable blueprint for instantiating neural networks with consistent architecture.

**Why this priority**: This is the foundational capability for the entire system. Without the ability to define network architectures through templates, users cannot create or train any neural networks. This represents the core value proposition of the educational tool.

**Independent Test**: Can be fully tested by creating a new network template with specified parameters (name, layer count, layer size, bias, activation function, loss function) and verifying the template is saved and displays correctly in the templates list.

**Acceptance Scenarios**:

1. **Given** the user opens the new network template dialog, **When** they specify a name, 3 layers, 10 neurons per layer, bias value 1.0, Sigmoid activation function, and Mean Squared Error loss function, **Then** a network template is created with these exact specifications.

2. **Given** the user opens the new network template dialog, **When** they leave the name field empty and attempt to create the template, **Then** the system highlights the name field as invalid and prevents template creation.

3. **Given** a network template has been created, **When** the user views the templates list, **Then** the template appears with its name and layer count displayed.

---

### User Story 2 - Customize Individual Layers in Template (Priority: P2)

A user wants to customize individual layers within a network template by modifying layer sizes and activation functions independently. This allows for heterogeneous network architectures where different layers have different characteristics.

**Why this priority**: This enables advanced network architectures beyond uniform configurations. While P1 allows creating templates with uniform layer configurations, this enables users to build more sophisticated architectures (e.g., encoder-decoder patterns, different activation functions per layer) which is essential for educational exploration.

**Independent Test**: Can be tested by creating a network template, then selecting individual layers in the tree view and modifying their size or activation function values, verifying each layer can be configured independently.

**Acceptance Scenarios**:

1. **Given** a network template with 3 layers exists, **When** the user selects the input layer and changes its size from 10 to 20 neurons, **Then** only the input layer size changes while other layers remain at 10 neurons.

2. **Given** a network template with uniform Sigmoid activation exists, **When** the user selects the output layer and changes its activation function to SoftMax, **Then** the output layer uses SoftMax while hidden layers continue using Sigmoid.

3. **Given** a network template exists, **When** the user views the template in the tree structure, **Then** each layer displays as "Input layer", "Layer N", or "Output layer" with child nodes showing size and activation function.

4. **Given** a layer's activation function is displayed, **When** the user double-clicks the activation function node, **Then** a dropdown appears with all available activation functions (Sigmoid, ReLU, TanH, SoftMax, GeLU, QuickGeLU, Linear, BinaryStep, Signum, TangentHyperbolicHard, Threshold).

---

### User Story 3 - Modify Global Template Properties (Priority: P2)

A user wants to modify global properties of a network template including the template name, bias value, and loss function. These properties apply uniformly across the entire network architecture.

**Why this priority**: Global properties affect the entire network's behavior and training characteristics. Being able to modify these after template creation allows users to experiment with different training configurations without recreating entire templates, supporting iterative learning and experimentation.

**Independent Test**: Can be tested by creating a network template, then editing its name, bias value, or loss function through the tree view, and verifying the changes are applied and persist.

**Acceptance Scenarios**:

1. **Given** a network template exists with name "Template 1", **When** the user edits the template name to "XOR Network", **Then** the template name updates to "XOR Network" in the templates list.

2. **Given** a network template exists with bias value 1.0, **When** the user edits the bias value to 0.5, **Then** the bias value changes to 0.5 for all layers in the network.

3. **Given** a network template exists with Mean Squared Error loss function, **When** the user edits the loss function to Binary Cross Entropy, **Then** the loss function changes to Binary Cross Entropy.

4. **Given** a loss function is displayed in the template tree, **When** the user double-clicks the loss function node, **Then** a dropdown appears with all available loss functions (MeanSquaredError, BinaryCrossEntropy, MeanBinaryCrossEntropy, HalfSquaredError).

---

### User Story 4 - Instantiate Network from Template (Priority: P1)

A user wants to create a trainable neural network instance from a network template. The instantiation process initializes weights according to the activation functions and creates a network ready for training.

**Why this priority**: This is the bridge between architecture definition (P1) and actual usage. Without the ability to instantiate networks from templates, the templates are unusable. This completes the minimum viable workflow: define architecture, create instance, train network.

**Independent Test**: Can be tested by selecting a network template, creating a new network instance with a unique name, and verifying the network appears in the networks list with properly initialized weights based on the template's specifications.

**Acceptance Scenarios**:

1. **Given** a valid network template exists, **When** the user creates a new network instance named "Network 1" from the template, **Then** a new network is created with the same architecture (layer sizes, activation functions, loss function, bias) as the template.

2. **Given** multiple network templates exist, **When** the user opens the new network dialog, **Then** a dropdown displays all available templates for selection.

3. **Given** a network is instantiated from a template, **When** the user views the network details, **Then** the network shows initialized weight matrices for each layer connection.

4. **Given** a network template with Sigmoid activation functions is used, **When** a network is instantiated, **Then** weights are initialized using He uniform initialization appropriate for the Sigmoid function.

---

### User Story 5 - Inspect Network Weights and Architecture (Priority: P3)

A user wants to visualize and inspect the weight matrices of an instantiated network. This includes viewing weights as both numerical tables and visual heat maps, allowing users to understand the network's learned parameters.

**Why this priority**: This is an educational and debugging feature that helps users understand what the network has learned. While not essential for basic network creation and training, visualization significantly enhances the learning experience and helps diagnose training issues.

**Independent Test**: Can be tested by selecting an instantiated network, viewing its weight matrices in the weights details panel, and verifying both tabular and heat map visualizations display correctly with adjustable zoom and gamma controls.

**Acceptance Scenarios**:

1. **Given** a trained network exists, **When** the user selects a weight matrix between layers, **Then** the weights display as both a numerical table and a color-coded heat map.

2. **Given** weights are displayed as a heat map, **When** the user adjusts the gamma slider, **Then** the heat map contrast adjusts to emphasize different weight value ranges.

3. **Given** weights are displayed as a heat map, **When** the user changes the zoom level, **Then** the visualization scales proportionally while maintaining aspect ratio.

4. **Given** a network with multiple layers exists, **When** the user views the weights details, **Then** they can select between different weight matrices corresponding to connections between each adjacent layer pair.

---

### User Story 6 - Delete Network Templates and Instances (Priority: P3)

A user wants to remove network templates or network instances that are no longer needed, cleaning up the workspace and removing obsolete configurations.

**Why this priority**: Workspace management is important for usability but not critical for core functionality. Users can work around this by ignoring unwanted items, though deletion improves organization and user experience.

**Independent Test**: Can be tested by creating a network template or instance, deleting it through the UI, and verifying it no longer appears in the respective list.

**Acceptance Scenarios**:

1. **Given** a network template exists, **When** the user selects the template and chooses delete, **Then** the template is removed from the templates list.

2. **Given** a network instance exists, **When** the user selects the network and chooses delete, **Then** the network is removed from the networks list.

3. **Given** a network template has instantiated networks, **When** the user deletes the template, **Then** the template is deleted but instantiated networks remain functional.

4. **Given** the user attempts to delete a layer from a template with only 2 layers, **When** the delete action is triggered, **Then** the system prevents deletion as templates require minimum 2 layers (input and output).

---

### Edge Cases

- What happens when a user attempts to create a network template with 0 or 1 layers (minimum 2 required: input and output)?
- How does the system handle extremely large layer sizes (e.g., 10,000 neurons) that may cause memory issues?
- What happens when a user changes a template's architecture after networks have been instantiated from it (does it affect existing instances)?
- How does the system handle weight initialization when layer sizes are very small (e.g., 1 neuron) or very large?
- What happens when a user attempts to set an invalid bias value (e.g., NaN, infinity)?
- How does the system behave when activation function-specific weight initialization fails?
- What happens when a network template name contains special characters or exceeds display width?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users to create network templates by specifying name, layer count, uniform layer size, bias value, activation function, and loss function
- **FR-002**: System MUST enforce minimum of 2 layers (input and output) in any network template
- **FR-003**: System MUST support at least the following activation functions: Sigmoid, ReLU, TanH, SoftMax, GeLU, QuickGeLU, Linear, BinaryStep, Signum, TangentHyperbolicHard, Threshold
- **FR-004**: System MUST support at least the following loss functions: Mean Squared Error, Binary Cross Entropy, Mean Binary Cross Entropy, Half Squared Error
- **FR-005**: System MUST allow independent modification of each layer's size and activation function after template creation
- **FR-006**: System MUST allow modification of global template properties (name, bias, loss function) after creation
- **FR-007**: System MUST persist network templates so they survive application restarts
- **FR-008**: System MUST allow users to instantiate multiple network instances from a single template
- **FR-009**: System MUST initialize network weights according to the activation function's preferred initialization strategy (e.g., He initialization for ReLU/Sigmoid, Xavier initialization for TanH/SoftMax)
- **FR-010**: System MUST support both Gaussian and Uniform weight initialization methods
- **FR-011**: System MUST display network templates in a hierarchical tree structure showing template name, loss function, bias, and all layers with their properties
- **FR-012**: System MUST label layers as "Input layer", "Layer N", or "Output layer" based on position
- **FR-013**: System MUST display weight matrices as both numerical tables and visual heat maps
- **FR-014**: System MUST provide zoom and gamma controls for weight visualization
- **FR-015**: System MUST prevent creation of templates with blank/empty names
- **FR-016**: System MUST allow deletion of network templates and instances independently
- **FR-017**: System MUST preserve existing network instances when their source template is deleted or modified
- **FR-018**: System MUST display activation functions using human-readable mathematical notation (e.g., "Sigmoid: 1 / (1 + exp[-v])")
- **FR-019**: System MUST display loss functions using mathematical notation (e.g., "MeanSquaredError: 1/n * sum([h - y]^2)")
- **FR-020**: System MUST organize weight matrices by layer index where layerWeights[i] contains weights from layer i to layer i+1

### Key Entities

- **Network Template**: Represents a reusable network architecture blueprint containing a name, ordered list of layer templates, loss function, and bias value. Templates define topology but not weights.

- **Layer Template**: Defines a single layer within a network template, specifying neuron count (size) and activation function. Layers are indexed from 0 (input) to n-1 (output).

- **Activation Function**: A transformation applied to layer outputs, implementing both forward pass (apply) and backward pass (derivative) computations. Each activation function specifies its preferred weight initialization strategy.

- **Loss Function**: Quantifies the difference between predicted and target outputs, computing both the network error (scalar) and output error gradient (vector) for backpropagation.

- **Network Instance**: A trainable neural network instantiated from a template, containing initialized weight matrices, layer sizes, activation functions, loss function, bias, and previous gradients for momentum-based optimization.

- **Weight Matrix**: A 2D array of connection weights between adjacent layers, where dimensions are (inputSize + 1) × outputSize, with the extra input dimension representing the bias node.

- **Weight Initializer**: A strategy for initializing network weights, including Gaussian (standard normal), Xavier (Gaussian or Uniform), and He (Gaussian or Uniform) initialization methods.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a network template with valid parameters in under 30 seconds
- **SC-002**: System supports all 11 specified activation functions (Sigmoid, ReLU, TanH, SoftMax, GeLU, QuickGeLU, Linear, BinaryStep, Signum, TangentHyperbolicHard, Threshold)
- **SC-003**: System supports all 4 specified loss functions (MSE, BCE, Mean BCE, Half Squared Error)
- **SC-004**: Network templates persist correctly across application restarts with 100% fidelity
- **SC-005**: Users can modify any template property (name, bias, loss function, layer size, layer activation) through the GUI within 10 seconds per property
- **SC-006**: Weight initialization completes in under 1 second for networks with up to 1000 neurons per layer
- **SC-007**: Weight visualization (heat map and table) renders in under 2 seconds for weight matrices up to 1000×1000
- **SC-008**: 90% of users can successfully create and instantiate their first network template within 5 minutes of using the system
- **SC-009**: System prevents all invalid configurations (empty names, fewer than 2 layers, null activation/loss functions) with clear validation feedback
- **SC-010**: Multiple networks instantiated from the same template operate independently with separate weight matrices
