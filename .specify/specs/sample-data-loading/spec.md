# Feature Specification: Sample Data Loading & Management

**Feature Branch**: `sample-data-loading`
**Created**: 2026-02-23
**Status**: Documentation
**Input**: Analysis of existing sample loading functionality in edu.yaprnn educational neural network application

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Load MNIST Digit Images for Training (Priority: P1)

As a student learning about neural networks, I need to load the MNIST handwritten digit dataset into the application so I can train a network to recognize digits. The system should automatically load the images and their corresponding labels from the IDX format files included with the application.

**Why this priority**: This is the core use case for the educational application. MNIST digit recognition is the foundational example for learning multilayer neural networks and must work independently for the application to have value.

**Independent Test**: Can be fully tested by importing the MNIST dataset from the bundled IDX files (digits.idx3-ubyte for images, digits.idx1-ubyte for labels), visualizing samples in the UI, and verifying that 10 digit classes (0-9) are correctly loaded with their labels.

**Acceptance Scenarios**:

1. **Given** the application is launched with bundled MNIST IDX files, **When** the user selects "Import Images" and provides paths to digits.idx3-ubyte (images) and digits.idx1-ubyte (labels), **Then** the system loads all samples with correct dimensions (28x28 pixels) and associates each image with its corresponding digit label (0-9).

2. **Given** successfully loaded MNIST samples, **When** the user views the samples list, **Then** each sample displays its name (generated from filename and index), label, and preview image in grayscale.

3. **Given** loaded MNIST samples, **When** the user selects a sample for detailed view, **Then** the system displays the original resolution (784 pixels for 28x28), allows zooming, and shows metadata including the source files and index position.

---

### User Story 2 - Load Audio Vowel Samples for Classification (Priority: P1)

As a student exploring different types of neural network inputs, I need to load vocal sound samples (vowels A, E, I, O, U) from AIFF files so I can train a network to classify sounds based on their frequency characteristics.

**Why this priority**: Audio classification demonstrates that neural networks work with different data types beyond images. This is independently valuable as an alternative learning path and shows the versatility of the multilayer perceptron approach.

**Independent Test**: Can be fully tested by selecting multiple AIFF files from the audio/ resource directory, verifying that the system extracts labels from filenames (a1-1.aiff = label "A"), performs FFT transformation to frequency domain, and displays frequency amplitude visualizations.

**Acceptance Scenarios**:

1. **Given** AIFF audio files in the resources/audio directory, **When** the user selects "Import Audio" and chooses one or more .aiff files, **Then** the system loads each file, extracts the label from the filename (first character), performs FFT to convert to frequency domain, and adds samples to the repository.

2. **Given** loaded audio samples, **When** the user views a sound sample, **Then** the preview displays a frequency amplitude visualization (heat-colored bars from base to peak) showing the spectral characteristics of the audio.

3. **Given** an audio sample preview, **When** the user clicks on the preview image, **Then** the system plays back the original audio file for verification.

---

### User Story 3 - Adjust Sample Resolution and Preprocessing (Priority: P2)

As a student learning about input preprocessing, I need to adjust the resolution and overlap parameters of samples so I can understand how input dimensionality affects network training and see the tradeoffs between detail and computational complexity.

**Why this priority**: Understanding preprocessing is crucial for neural network education, but students can initially learn with default parameters. This feature enables deeper exploration of how input representation affects learning.

**Independent Test**: Can be tested by loading any sample type, adjusting the resolution spinner (changes number of input neurons) and overlap spinner (controls window overlap during subsampling), and observing real-time preview updates showing the processed input that will feed the network.

**Acceptance Scenarios**:

1. **Given** a loaded image sample with original resolution 784 (28x28), **When** the user adjusts the resolution spinner to 400, **Then** the system resamples the image using overlapping windows and displays the processed preview at the new resolution with updated metadata.

2. **Given** a loaded audio sample, **When** the user increases the overlap parameter from 0.0 to 0.5, **Then** the system recalculates the frequency amplitude subsampling with 50% window overlap and updates the preview visualization.

3. **Given** adjusted preprocessing parameters, **When** the user changes the zoom level, **Then** both the original and processed previews scale proportionally for easier visual comparison.

---

### User Story 4 - View Sample Metadata and Source Information (Priority: P2)

As a student verifying my data, I need to see detailed metadata about each loaded sample including source files, resolution, label, and index position so I can understand the data provenance and verify correct loading.

**Why this priority**: Metadata visibility supports educational understanding and debugging, but is secondary to the core loading and visualization functionality.

**Independent Test**: Can be tested by selecting any loaded sample and verifying that the details panel shows all relevant information in HTML table format: source file paths, sample name, assigned label, original resolution, and input resolution after preprocessing.

**Acceptance Scenarios**:

1. **Given** a selected MNIST image sample, **When** the user views the sample details, **Then** the system displays a metadata table showing: Images Package File path, Labels Package File path, sample name (e.g., "digits.idx3-ubyte_42"), label ("7"), original resolution (784), and current input resolution.

2. **Given** a selected audio sample, **When** the user views the sample details, **Then** the metadata shows: Audio File path, name (filename), label (extracted vowel), original resolution (frequency samples), and input resolution (after subsampling).

3. **Given** any sample type, **When** preprocessing parameters change, **Then** the metadata table updates to reflect the new input resolution while preserving original resolution information.

---

### User Story 5 - Validate Data File Formats and Handle Errors (Priority: P3)

As a user loading external data files, I need the system to validate IDX file formats and AIFF audio formats so I receive clear error messages if files are corrupted or incorrect rather than experiencing crashes or silent failures.

**Why this priority**: Error handling improves user experience but is not critical for the educational mission. Users typically work with bundled, validated datasets.

**Independent Test**: Can be tested by attempting to load files with incorrect magic numbers (not 2051 for images, not 2049 for labels), mismatched image/label counts, truncated files, or invalid AIFF data, and verifying that the system displays appropriate error dialogs.

**Acceptance Scenarios**:

1. **Given** an invalid images package file with wrong magic number, **When** the user attempts to import it, **Then** the system displays an error message "File [path] is not an image package" and does not add corrupted data to the repository.

2. **Given** images and labels packages with mismatched counts, **When** the user imports them, **Then** the system displays an error "Count of images X does not match count of labels Y" and aborts the import.

3. **Given** a truncated or invalid AIFF file, **When** the user attempts to import it, **Then** the system displays an error "File [path] seems to be an invalid AIFF file" and does not add the sample.

---

### Edge Cases

- What happens when attempting to load MNIST files with dimensions other than 28x28?
  - System reads dimensions from IDX header (stored at bytes 8-15) and adapts to any height/width combination

- How does the system handle loading extremely large datasets (e.g., full MNIST 60,000 samples)?
  - All samples are loaded into memory as Sample objects; performance may degrade with very large datasets but current architecture supports it

- What happens if user selects resolution value of 1 or tries to set resolution higher than original?
  - Resolution is clamped: minimum 1 pixel, maximum (original_width * original_height) for images; for audio, controlled by formula

- How does subsampling handle edge cases at image boundaries?
  - Uses Math.max/min to clamp window coordinates to valid ranges [0, width) and [0, height), ensuring no out-of-bounds access

- What happens if audio filename doesn't match expected pattern (letter + digit + hyphen + digit)?
  - Label extracted as uppercase first character; if filename is invalid, label may be incorrect but system doesn't crash

- How are labels validated against expected label sets (0-9 for digits, A/E/I/O/U for vowels)?
  - ImageSample.LABELS = ["0".."9"], SoundSample.LABELS = ["A","E","I","O","U"]; target vectors use indexOf, which returns -1 if label not found (edge case: would create invalid target vector)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support loading MNIST digit images from IDX format files (IDX3 for images with magic number 2051, IDX1 for labels with magic number 2049)

- **FR-002**: System MUST support loading audio samples from AIFF format files and automatically extract labels from filenames

- **FR-003**: System MUST validate IDX file headers and reject files with incorrect magic numbers or corrupted data

- **FR-004**: System MUST normalize pixel values from raw byte range [0-255] to float range [0.0-1.0] for image data

- **FR-005**: System MUST perform Fast Fourier Transform (FFT) on audio samples to convert time-domain waveforms to frequency-domain amplitude representations

- **FR-006**: System MUST convert labels to one-hot encoded target vectors based on the predefined label set for each sample type

- **FR-007**: System MUST support variable resolution subsampling of images using overlapping window averaging

- **FR-008**: System MUST support variable resolution subsampling of audio frequency data using exponentially increasing window widths (lambda parameter)

- **FR-009**: System MUST maintain both original and preprocessed (input) versions of sample data for comparison and visualization

- **FR-010**: System MUST generate preview images from sample data: grayscale for images, heat-colored amplitude bars for audio

- **FR-011**: System MUST support zoom levels for sample preview visualization (configurable scaling)

- **FR-012**: System MUST play back audio samples when user clicks on audio sample preview

- **FR-013**: System MUST display comprehensive metadata for each sample including source files, dimensions, label, and index

- **FR-014**: System MUST provide file chooser dialogs with appropriate file filters (.idx1-ubyte, .idx3-ubyte, .aiff)

- **FR-015**: System MUST validate file paths exist before attempting import and provide visual feedback (background color) on validation status

- **FR-016**: System MUST add successfully loaded samples to the repository for use in training data creation

- **FR-017**: System MUST match image count with label count during IDX import and reject mismatched packages

### Key Entities

- **Sample**: Abstract interface representing any trainable data point with file, name, label, target vector, input array, original array, and preview generation methods. Three concrete implementations: ImageSample, SoundSample, SimpleSample.

- **ImageSample**: MNIST-style image sample with 28x28 pixel grayscale data (or variable dimensions read from IDX header), labels "0" through "9", supports subsampling with overlapping rectangular windows for resolution reduction.

- **SoundSample**: Audio frequency sample with labels "A", "E", "I", "O", "U" (vowels), stores FFT-transformed frequency amplitudes, supports subsampling with exponentially increasing window widths for perceptually-appropriate frequency binning.

- **SimpleSample**: Generic sample builder for programmatic sample creation (not used in file import workflows).

- **ImagesImportService**: Service for loading IDX format image and label packages, validates magic numbers (2051/2049), reads dimensions and counts from headers, creates ImageSample instances with normalized pixel data.

- **AudiosImportService**: Service for loading AIFF audio files, performs FFT transformation to frequency domain, extracts labels from filenames, creates SoundSample instances with subsampled frequency amplitudes.

- **ImportService**: Base service providing label-to-target-vector conversion (one-hot encoding), shared by both import services.

- **Repository**: Application-wide sample storage, maintains collections of samples, training data, networks, and templates for user session.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can successfully import the bundled MNIST dataset (50,000+ digit images) and verify that all 10 digit classes (0-9) are represented with correct labels

- **SC-002**: Users can load audio samples for all 5 vowel categories (A, E, I, O, U) and visually confirm distinct frequency patterns in the amplitude previews

- **SC-003**: Sample preview images accurately represent the original data (grayscale pixel values for images, frequency spectra for audio) as verified by visual inspection against source files

- **SC-004**: Subsampling operations complete in under 1 second for individual samples at typical resolutions (400-800 inputs) as measured on standard hardware

- **SC-005**: Resolution and overlap parameter changes update preview visualizations in real-time (under 200ms latency) for responsive user experimentation

- **SC-006**: Invalid file imports display clear, actionable error messages 100% of the time rather than crashing or silently failing

- **SC-007**: Audio playback triggers correctly when clicking on audio sample previews with audio stream properly opening and closing

- **SC-008**: Preprocessed sample dimensions match the configured resolution parameter exactly, as verified in metadata display and downstream network input layer sizing

- **SC-009**: The sample loading interface requires no more than 3 clicks (File > Import Images/Audio > Select Files > OK) to complete import workflow

- **SC-010**: Sample metadata displays all required information fields (source files, name, label, resolutions) with no null or missing values for successfully loaded samples

## Technical Notes

### IDX File Format

The IDX file format is used by the MNIST database:
- Magic number (4 bytes): 2051 for images (IDX3), 2049 for labels (IDX1)
- Number of items (4 bytes): count of images or labels
- For images: number of rows (4 bytes), number of columns (4 bytes)
- Data: unsigned bytes for pixel values (0-255) or label values (0-9)

### Normalization Formula

Pixel bytes are converted to floats using:
```
normalized_value = (unsigned_byte_value) / 255.0
```
Where unsigned byte conversion handles Java's signed bytes: `byte >= 0 ? byte : 128 + (byte & 0x7F)`

### Audio Processing Pipeline

1. Load AIFF file using javax.sound.sampled.AudioSystem
2. Convert raw bytes to wave samples: `sample = SHORT_NORM * (byte1 << 8 | byte2 & 0xff)` where `SHORT_NORM = -1.0 / Short.MIN_VALUE`
3. Apply FFT using JTransforms FloatFFT_1D.realForwardFull()
4. Calculate frequency amplitudes: `amplitude = sqrt(real^2 + imaginary^2)`
5. Subsample frequency amplitudes using exponentially increasing windows

### Subsampling Algorithms

**Image Subsampling**: Uses overlapping rectangular windows that average pixels. Window positions are calculated to achieve target resolution while maintaining aspect ratio. Overlap parameter controls window extension in all directions.

**Audio Subsampling**: Uses exponentially increasing window widths to match human frequency perception (log scale). Initial window width calculated by: `original_length * (1 - lambda) / (1 - lambda^resolution)` where lambda > 1.0 (typically 1.005).

### Preview Generation

- **Images**: Convert normalized float array to BufferedImage TYPE_INT_RGB by scaling [0.0-1.0] to [0-255] and creating grayscale RGB values
- **Audio**: Create BufferedImage 256px height, width = number of frequency samples, use heat color scheme (COLOR_BASE - scaling * frequency_amplitude) to visualize spectrum

### File Path Handling

The system supports both URL-based loading (for bundled resources) and File-based loading (for user-provided files) with dual method signatures in ImagesImportService.
