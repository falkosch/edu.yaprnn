# Network / MLP Algorithm Issues

## N1: SoftMax derivative is only correct with cross-entropy loss

**File**: `src/main/java/edu/yaprnn/networks/activation/SoftMaxActivationFunction.java:7-13`
**Category**: Network

**Problem**: The SoftMax derivative implementation uses the diagonal approximation `h[i] * (1 - h[i])`, which is only correct when paired with cross-entropy loss. The code documents this in a comment (lines 7-13), but there is no runtime validation preventing SoftMax from being paired with squared-error loss functions, which would produce incorrect gradients.

**Suggestion**: Add a validation check when creating a network to warn or prevent SoftMax + non-cross-entropy combinations. This could be in `MultiLayerNetworkTemplate` or during network creation.

## N2: BinaryStep and Signum use surrogate derivative of 1.0

**File**: `src/main/java/edu/yaprnn/networks/activation/BinaryStepActivationFunction.java:24`
**File**: `src/main/java/edu/yaprnn/networks/activation/SignumActivationFunction.java:25`
**Category**: Network

**Problem**: Both activation functions have zero derivative everywhere except at the discontinuity. Using a surrogate constant derivative of 1.0 allows gradient flow but is mathematically incorrect and may cause training instability. This is a known trade-off for educational purposes.

**Suggestion**: Document this as a "straight-through estimator" (STE) and note that it's an approximation for educational purposes.

## N3: BCE loss functions use exact float equality check for zero guard

**File**: `src/main/java/edu/yaprnn/networks/loss/BinaryCrossEntropyLossFunction.java:23`
**File**: `src/main/java/edu/yaprnn/networks/loss/MeanBinaryCrossEntropyLossFunction.java:24`
**Category**: Network

**Problem**: Both compute `xMxx = x - x * x` (i.e., `x(1-x)`) and guard against division by zero with `xMxx == 0f`. With float precision, values very close to 0 or 1 may produce `xMxx` that is not exactly zero but extremely small, leading to huge gradient magnitudes that destabilize training.

**Suggestion**: Use an epsilon comparison: `Math.abs(xMxx) < 1e-7f ? 0f : (x - y) / xMxx`, or clamp the output values before computing the gradient.

## N4: BCE gradient assumes sigmoid activation without validation

**File**: `src/main/java/edu/yaprnn/networks/loss/BinaryCrossEntropyLossFunction.java:13-26`
**Category**: Network

**Problem**: The BCE gradient formula `(x - y) / (x(1-x))` is the simplified form when combined with sigmoid activation. If paired with a non-sigmoid output activation, the gradient is mathematically incorrect but the code silently produces wrong results.

**Suggestion**: Add a note in the documentation, similar to the one in `SoftMaxActivationFunction`, or add a runtime validation when creating networks.
