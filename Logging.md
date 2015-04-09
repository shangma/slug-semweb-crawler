# Logging in Slug #

Slug uses the Java Logging API to generate its logs.

Logging is configured through a logging.properties file. A sample file is included in the distribution and the command-line scripts automatically reference that file, using:

```
-Djava.util.logging.config.file=etc/logging.properties
```

Refer to the standard Java documentation for further details of how to configure logging for different purposes.

# Logging Levels #

In the code, care has been taken to attempt to include reasonable amounts of information at each of the different standard java logging levels. The information included at each level is summarized below:

|Level|Description|
|:----|:----------|
|SEVERE|Severe errors, either during application execution or errors during RDF parsing|
|WARNING|Primarily warnings from the RDF parser. Slug generates few warnings itself.|
|CONFIG|Confirmation of specific configuration elements. These are generally logged at startup|
|INFO|Information messages, e.g. high-level application events, summary of crawl progress (every 1000 urls). This is the default logging level and suitable for most purposes|
|FINE|Individual task events: starting, success, failure of individual tasks. Queueing of new tasks following completion of an existing task|
|FINER|Loading and saving of files, component creation|
|FINEST|Trace level debugging, e.g. progress reports for each task, individual threads starting and stopping, etc.|

When running a crawler profile that does not include a crawler memory (i.e. there is no slug:hasMemory property associating a crawler with a slug:Memory instance) then the recommended logging level is FINE as this provides some useful additional information on task failures, etc. Otherwise INFO level should be sufficient.