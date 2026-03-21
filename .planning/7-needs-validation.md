# Needs Validation

## V1: ClassifyFrame classify() does not null-check selections

**File**: `src/main/java/edu/yaprnn/gui/views/ClassifyFrame.java:179-181`
**Category**: Java

**Problem**: `classify()` gets selections from combo box models without null checks. If no sample or network is selected, `feedForward` will throw NPE. The button is disabled when nothing is selected (line 240), but a race condition could occur if the selection changes between the enable check and the button click.

**Validation needed**: Verify whether the enable/disable guard is sufficient, or add null checks in `classify()`.

## V2: SoundSample.initialWindowWidthFrom may overflow for large resolution

**File**: `src/main/java/edu/yaprnn/samples/model/SoundSample.java:74-76`
**Category**: Java

**Problem**: `Math.pow(lambda, resolution)` could overflow to `Infinity` for large resolution values, leading to `NaN` propagation in downstream computations.

**Validation needed**: Check if the UI constrains resolution values to a safe range. If not, add bounds checking.

## V3: ShuffleService normalization silently changes split ratios

**File**: `src/main/java/edu/yaprnn/training/ShuffleService.java:28`
**Category**: Java

**Problem**: `Math.max(1.0, trainingPercentage + devTestPercentage)` normalizes when the sum exceeds 1.0. If both percentages are large (e.g., 0.8 and 0.8), they are silently rescaled to 50%/50% instead of warning the user.

**Validation needed**: Confirm whether the UI spinner constraints prevent this case, or add validation with user feedback.
