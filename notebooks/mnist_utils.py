import os
import pathlib
import random
import struct
import typing

import matplotlib.colors
import matplotlib.pyplot as plt
import numpy as np
import numpy.typing as npt

IMAGES_PACKAGE_HEADER_SIGNATURE = 2051
LABELS_PACKAGE_HEADER_SIGNATURE = 2049
HEADER_SIZE_IMAGES = 16
HEADER_SIZE_LABELS = 8


class ImageSample:
    def __init__(self, image_path: pathlib.Path, label_path: pathlib.Path, index: int, name: str, label: str | int,
                 target: npt.NDArray, width: int, height: int, original: npt.NDArray):
        self.image_path = image_path
        self.label_path = label_path
        self.index = index
        self.name = name
        self.label = str(label)
        self.target = target
        self.width = width
        self.height = height
        self.original = original


def read_labels(label_path: pathlib.Path) -> list[int]:
    with open(label_path, 'rb') as file:
        magic_number, count_labels = struct.unpack('>II', file.read(HEADER_SIZE_LABELS))
        if magic_number != LABELS_PACKAGE_HEADER_SIGNATURE:
            raise ValueError(f'File {label_path} is not a valid label package')
        return list(struct.unpack(f'>{count_labels}B', file.read(count_labels)))


def read_image_chunk(stream: typing.BinaryIO, image_size: int, image_path: pathlib.Path) -> bytes:
    data = stream.read(image_size)
    if len(data) != image_size:
        raise ValueError(f"Unexpected EOF reached in {image_path}. Image data is malformed.")
    return data


def read_image_header(image_path: pathlib.Path) -> tuple[int, int, int]:
    with open(image_path, 'rb') as file:
        magic_number, count_images, height, width = struct.unpack('>IIII', file.read(HEADER_SIZE_IMAGES))
        if magic_number != IMAGES_PACKAGE_HEADER_SIGNATURE:
            raise ValueError(f'File {image_path} is not a valid image package')
        return count_images, height, width


def read_images(images_path: pathlib.Path, labels_path: pathlib.Path, labels: list[int], unique_labels: list[int]) -> \
        list[ImageSample]:
    count_images, height, width = read_image_header(images_path)
    if count_images != len(labels):
        raise ValueError(f'Count of images ({count_images}) does not match count of labels ({len(labels)})')
    image_size = height * width
    with open(images_path, 'rb') as file:
        file.seek(HEADER_SIZE_IMAGES)  # Skip header
        samples = []
        for i, label in enumerate(labels):
            name = f"{images_path.stem}_{i}"
            raw_data = read_image_chunk(file, image_size, images_path)
            processed_data = np.frombuffer(raw_data, dtype=np.uint8).astype(np.float32) / 255.0
            target = np.array([1 if label == unique_label else 0 for unique_label in unique_labels])
            samples.append(ImageSample(images_path, labels_path, i, name, label, target, width, height, processed_data))
        return samples


def load_image_and_label_data(images_file: str, labels_file: str) -> list[ImageSample]:
    labels_path = pathlib.Path(labels_file)
    images_path = pathlib.Path(images_file)
    all_labels = read_labels(labels_path)
    all_unique_labels = sorted(set(all_labels))
    return read_images(images_path, labels_path, all_labels, all_unique_labels)


def plot_image_sample(image_sample: ImageSample, cmap: str | matplotlib.colors.Colormap = 'gray'):
    image_data = image_sample.original.reshape(image_sample.height, image_sample.width)
    plt.figure(figsize=(1, 1))
    plt.imshow(image_data, cmap=cmap, vmin=np.min(image_data), vmax=np.max(image_data))
    plt.title(f'Name: {image_sample.name} | Label: {image_sample.label}')
    plt.axis('off')
    plt.tight_layout()
    plt.show()


def plot_image_samples(image_samples: list[ImageSample], cmap: str | matplotlib.colors.Colormap = 'gray'):
    image_sample = image_samples[0]
    images = np.array([image_sample.original for image_sample in image_samples])
    return plot_images(images, image_size=(image_sample.height, image_sample.width), cmap=cmap)


def plot_images(images: npt.NDArray, image_size: tuple[int, int], cmap: str | matplotlib.colors.Colormap = 'gray'):
    num_samples = images.shape[0]
    fig, axes = plt.subplots(1, num_samples)
    if num_samples == 1:
        axes = [axes]
    for ax, image in zip(axes, images):
        ax.imshow(image.reshape(image_size), cmap=cmap, vmin=np.min(image), vmax=np.max(image))
        ax.axis('off')
    plt.show()


def from_mnist_images(images_file: str = '../src/main/resources/digits.idx3-ubyte',
                      labels_file: str = '../src/main/resources/digits.idx1-ubyte'):
    print(f'Loading MNIST images from {os.path.abspath(images_file)}')

    mnist_images = load_image_and_label_data(images_file, labels_file)
    plot_image_sample(mnist_images[0])
    plot_image_samples(mnist_images[:3])
    plot_image_samples(random.sample(mnist_images, 10))

    image_size = (mnist_images[0].height, mnist_images[0].width)

    x = np.array([i.original for i in mnist_images])
    random_x_indices = np.random.choice(x.shape[0], size=10, replace=False)
    random_feature_indices = np.random.choice(x.shape[1], size=10, replace=False)

    y = np.array([i.target for i in mnist_images])
    labels = np.array([i.label for i in mnist_images])

    return mnist_images, image_size, x, random_x_indices, random_feature_indices, y, labels


def from_dataset(dataset, explain=False):
    x_train, x_test, y_train, y_test, labels_train, labels_test = dataset
    if explain:
        print(x_train.shape, y_train.shape, labels_train.shape, x_test.shape, y_test.shape, labels_test.shape)
    return x_train.shape[1], y_train.shape[1], x_train, y_train, x_test, y_test
