Concurrency test are based on [OpenJDK Java Concurrency Stress tests](https://wiki.openjdk.java.net/display/CodeTools/jcstress).

### Command line launching

Build tests jar with maven and run tests:
````bash
mvn clean install
java -jar target/jcstress.jar
````

Look at results report `results/index.html`

### Command line options

The whole list of command line options is available by:

    java -jar target/jcstress.jar
