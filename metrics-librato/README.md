## Librato Metrics Plugin for the Metrics Library

The `LibratoReporter` class runs in the background, publishing metrics to the <a href="http://metrics.librato.com">Librato Metrics API</a> at the specified interval.

## Usage

During the initialization of your program, simply use the `.enable` method with the appropriately configured `LibratoReporter.Builder` class. See the setters on that method for all the available customizations (there are quite a few). The constructor for the `Builder` requres only the things that are necessary; sane defaults are provided for the rest of the options.

    LibratoReporter.enable(LibratoReporter.builder("<Librato Username>", "<Librato API Token>", "<Source Identifier (usually hostname)>"), 10, TimeUnit.SECONDS);

