import sys

import numpy as np
import matplotlib.pyplot as plt
from scipy import stats


def draw_histogram(data, binwidth = 1):
    plt.hist(data, bins=np.arange(min(data), max(data) + binwidth, binwidth))
    axes = plt.gca()
    axes.set_xlim([0,2000])
    axes.set_ylim([0,600])
    plt.show()


def build(data, binwidth = 1):
    return np.histogram(data, bins=np.arange(min(data), max(data) + binwidth, binwidth))


def write_histogram_file(name, data):
    with open(name, 'w') as file:
        file.write('bin\tvalue\n')
        for i in range(len(data[0])):
            file.write("{}\t{}\n".format(data[1][i + 1], data[0][i]))


path = "../../../files/stuttgart_bus_distances.txt"
entries = []
with open(path) as reader:
    for line in reader:
        entries.append(float(line))

write_histogram_file("../../../files/stuttgart_bus_distances.histogram.txt", build(entries, 2))
