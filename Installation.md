# Installation #

Installation is quite straight-forward. All required libraries and code are provided in the distribution for each release, so simply:

  1. Download the desired release. Ensure that you save the file locally as some users have reported problems opening the file directly in a browser.
  1. Unzip the distribution, e.g. in your home directory. You'll end up with a new sub-directory slug containing all code and files.
  1. You may want to create a new environment variable $SLUG\_HOME or %SLUG\_HOME% to refer to the location of the scutter. You'll probably also want to add this directory to your PATH.
  1. Ensure you have Java 1.6 installed, you'll need this to run the scutter
  1. It's also recommended that you install Apache Ant. You'll definitely need this if you want to contribute to the project, locally customise the code, or build it yourself. The provided Ant script ($SLUG\_HOME/build.xml) also includes a few helpful tools such as building the javadocs, etc. See the tools documentation for notes on that.
  1. That's it, you're ready to configure and run a scutter

# Running the Scutter #

The Slug distribution includes shell scripts for running a scutter. Run `$SLUG_HOME/slug.sh` or `slug.bat` depending on your platform. These scripts configure the required Java CLASSPATH.

These scripts accept the following parameters. The majority are required:

|Parameter|Purpose|Required?|
|:--------|:------|:--------|
|-config|Path to a Slug configuration file|Yes|
|-id|Identifier for scutter profile as defined in the above config. file|Yes|
|-plan|Path to a "scutter plan". i.e an RDF document identifying the list of initial URLs to be crawled. The distribution includes a simple example, sample-plan.rdf|No, supply this or -freshen, or both|
|-freshen|Indicates whether the scutter should add all previously found URLs to its initial crawler plan. Used to "freshen" already discovered data|No, supply this or -plan, or both|

For very large crawls, it may be necessary to allocate more memory to the JVM. This is particularly true if the crawler is using an in-process memory. To do this edit the shell script or batch file to set the JVM heap size using the -Xmx parameter. E.g to allocate 1Gb of memory to the JVM add the following:

```
-Xmx1024m
```

# Stopping the Scutter #

The crawler will automatically stop after it has completed all tasks in its current queue. The log files summarize the number of tasks completed and remaining after every 1000 tasks, so this is the ideal place to monitor the progress of a crawl.

In some cases, e.g. where a crawl is unbounded, it may be necessary to kill the crawler manually.

This can be done by simply hitting Ctrl+C in the command window running the crawler. If the crawler is being run as a background process on a **nix system, then the crawler can be cleanly shutdown using:**

```
kill -SIGTERM pid
```

Where "pid" is the process id of the JVM running the crawler.