import sys

import numpy as np


def read_report(path):
    entries = []

    with open(path) as reader:
        for line in reader:
            values = line.split('\t')
            entries.append((float(values[1]), float(values[2]), float(values[3])))

    return entries


def average_fd_histogram(dataset):
    data = [e[2] for e in dataset]
    return np.histogram(data, bins=[0, 5, 10, 20, 40, 80, 320, 640, 1280, 2560, 10240])


def an_histogram(dataset):
    data = [e[0] for e in dataset]
    return np.histogram(data, bins=20, range=(0, 1))


def al_histogram(dataset):
    data = [e[1] for e in dataset]
    return np.histogram(data, bins=20, range=(0, 1))


def accuracy(dataset):
    data = [e[0] for e in dataset]
    acc = [0 for i in range(10)]
    for entry in data:
        for i in range(10):
            if entry <= i * 0.1:
                acc[i] += 1

    for i in range(10):
        acc[i] /= len(data)

    return np.array(acc), np.arange(0.0, 1.1, 0.1)


def write_histogram_file(name, data):
    with open(name, 'w') as file:
        file.write('bin\tvalue\n')
        for i in range(len(data[0])):
            file.write("{:.2g}\t{}\n".format(data[1][i + 1], data[0][i]))


def main(report, output):
    report = read_report(report)

    avg_fd = average_fd_histogram(report)
    acc = accuracy(report)
    an = an_histogram(report)
    al = al_histogram(report)

    with open(output + '.stats.avg_fd.tsv', 'w') as file:
        file.write('bin\tvalue\n')
        for i in range(len(avg_fd[0])):
            file.write("{:}\t{}\n".format(avg_fd[1][i + 1], avg_fd[0][i]))

    write_histogram_file(output + '.stats.an.tsv', an)
    write_histogram_file(output + '.stats.al.tsv', al)
    write_histogram_file(output + '.stats.accuracy.tsv', acc)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print('Usage: ' + sys.argv[0] + ' <report> <output>')
        exit(0)

    main(sys.argv[1], sys.argv[2])
