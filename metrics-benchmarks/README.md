Benchmarks are based on [Oracle Java Microbenchmark Harness](http://openjdk.java.net/projects/code-tools/jmh/).

### Results interpretation

Please be cautious with conclusions based on microbenchmarking as there are plenty possible pitfalls for goals, test compositions, input data, an envionment and the analyze itself.

### Command line launching

Execute all benchmark methods with 4 worker threads:

    mvn clean install
    java -jar target/benchmarks.jar  ".*" -t 4

or specify a filter for benchmark methods and the number of forks and warmup/measurements iterations, e.g.:

    java -jar target/benchmarks.jar  -t 4  -f 3 -i 10 -wi 5  ".*CounterBenchmark.*"
    java -jar target/benchmarks.jar  -t 4  -f 3 -i 10 -wi 5  ".*ReservoirBenchmark.*"
    java -jar target/benchmarks.jar  -t 4  -f 3 -i 10 -wi 5  ".*MeterBenchmark.*"

### Command line options

The whole list of command line options is available by:

    java -jar target/benchmarks.jar -h
