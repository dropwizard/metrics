# mvn package
CP=$(find target|grep jar$|grep -v sources|grep -v javadoc  | tr '\n' ':'):target/test-classes

echo CP: $CP
java -cp $CP org.openjdk.jmh.Main com.codahale.metrics.BenchmarkNewEDR > new.txt
java -cp $CP org.openjdk.jmh.Main com.codahale.metrics.BenchmarkOldEDR > old.txt

