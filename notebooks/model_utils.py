import typing as tp

import matplotlib.pyplot as plt
import numpy as np
import numpy.typing as npt

SEED = 42
BATCH_SIZE = 32
LEARNING_RATE = 0.02
BATCH_LEARNING_RATE = LEARNING_RATE / BATCH_SIZE
L1_DECAY = 0.001
L2_DECAY = 0.001
MOMENTUM = 0.2
NESTEROV_MOMENTUM = True

CLASSIFICATION_EPOCHS = 50
CLASSIFICATION_FEATURES = [20, 15]
AUTOENCODER_EPOCHS = 100
AUTOENCODER_FEATURES = [196, 49, 12, 49, 196]


def mse_loss(_model: tp.Callable[..., tp.Any], X: npt.NDArray, y: npt.NDArray):
    return np.mean((_model(X) - y) ** 2)


def plot_metric(values: npt.NDArray, range_label='Range', values_label='Values'):
    plt.plot(range(len(values)), values, label=values_label)
    plt.xlabel(range_label)
    plt.ylabel(values_label)
    plt.legend()
    plt.show()


def plot_training_metrics(loss: npt.NDArray, accuracy: npt.NDArray | None = None):
    fig, ax1 = plt.subplots()
    ax1.plot(range(len(loss)), loss, label='Loss', color='tab:red')
    ax1.set_xlabel('Steps')
    ax1.set_ylabel('Loss', color='tab:red')
    ax1.tick_params(axis='y', labelcolor='tab:red')
    ax1.legend(loc='upper left')
    if accuracy is not None:
        ax2 = ax1.twinx()
        ax2.plot(range(len(accuracy)), accuracy, label='Accuracy', color='tab:green')
        ax2.set_ylabel('Accuracy', color='tab:green')
        ax2.tick_params(axis='y', labelcolor='tab:green')
        ax2.legend(loc='upper right')
    plt.title('Training Metrics')
    plt.show()


def calculate_accuracy(predict: tp.Callable[..., any], X: any, y: npt.NDArray):
    y_pred = np.argmax(predict(X), axis=1)
    y_true = np.argmax(y, axis=1)
    return np.mean(y_true == y_pred)


def create_batches(X: npt.NDArray, y: npt.NDArray, batch_size: int) -> tp.Iterable[tp.Tuple[any, any]]:
    indices = np.arange(X.shape[0])
    np.random.shuffle(indices)
    shuffled_X = X[indices]
    shuffled_y = y[indices]
    for i in range(X.shape[0] // batch_size):
        yield shuffled_X[i * batch_size:(i + 1) * batch_size], shuffled_y[i * batch_size:(i + 1) * batch_size]
