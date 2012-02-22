# Sigar Integration #

An optional extension to Metrics that uses the [Hyperic Sigar](http://support.hyperic.com/display/SIGAR/Home) library to provide more information about the JVM process and the machine on which it is running.

## Usage ##

1. Add a dependency on metrics-sigar to your project:

        <dependency>
            <groupId>com.yammer.metrics</groupId>
            <artifactId>metrics-sigar</artifactId>
            <version>${metricsVersion}</version>
        </dependency>

2. Download the Sigar native libraries. 

    * If downloading manually, download the Sigar binary package from SourceForge [here](http://sourceforge.net/projects/sigar/files/sigar/1.6/). 

    * If you want to automate it in your build script, add the following dependency to your project and add a task to unzip the jar.

            <dependency>
                <groupId>org.fusesource</groupId>
                <artifactId>sigar</artifactId>
                <version>1.6.4</version>
                <classifier>native</classifier>
            </dependency>

3. Make sure Sigar can find its native libs at runtime. Either:

    * Ensure the libs are in the same directory as `sigar.jar`, or

    * Set the system property `-Dorg.hyperic.sigar.path=<directory containing native libs>`

4. You're ready to use metrics-sigar! 

    * Use the `SigarMetrics` singleton object directly:

            SigarMetrics sm = SigarMetrics.getInstance();

    * Optionally expose the most useful metrics as `Gauge`s:

            SigarMetrics.getInstance().registerGauges();

## Acknowledgements ##

This module makes use of [Hyperic Sigar](http://support.hyperic.com/display/SIGAR/Home) ([Github link](https://github.com/hyperic/sigar)) under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
