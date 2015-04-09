# The Configuration File #

Slug requires a configuration file in order to configure a number of settings that describe how the crawler will operate. Collectively these settings are known as a profile.

These settings include details such as:

  * The number of threads active during the crawl
  * Where/how the crawler stores its memory
  * Which components will process retrieved data, e.g. to persist it
  * How the crawler will apply filters (if any) to newly found URLs

The Slug distribution includes a sample config file config.rdf that demonstrates how to configure all of the current components.

The configuration file is expressed as RDF/XML. A given configuration file may contain entries for more than one profile. Therefore when running the scutter one must provide the identifier of a Scutter described in the configuration. This is specified with the -id parameter, see Running the Scutter.

# The Configuration Schema #

The complete schema for the Scutter configuration is available in `etc/schema/config.rdfs` in the distribution. It is also available online

The namespace URI is `http://purl.org/NET/schemas/slug/config/`.

The preferred namespace prefix is `slug`.

The following sections describe some of the key classes and relationships.

## Scutter ##

The slug:Scutter class describes an individual crawler. A given configuration file may describe more than one crawler.

# Configuration Example #

See http://www.ldodds.com/projects/slug/config.rdf for example configurations. This sample file contains the following Scutter configurations:

### default ###
A default Scutter configuration

Memory: a file-based memory named memory.rdf
Workers: 10 threads

Consumers:
  * storer - stores HTTP responses in c:\temp\slug-cache
  * rdf-consumer - looks for rdfs:seeAlso links in scuttered content and adds them to the scutter task list.

Filters:
  * single-fetch-filter - avoids loops by remembering which resources have been fetched
  * depth-filter - limits crawl depth to 3 links deep
  * regex-filter - blocks all URLs containing the word "livejournal"

### shallow-slow-scutter ###
A Scutter which crawls only a shallow depth, with fewer workers

> Memory: a file-based memory named memory.rdf
Workers: 3 threads

Consumers:
  * storer - stores HTTP responses in c:\temp\slug-cache
  * rdf-consumer - looks for rdfs:seeAlso links in scuttered content and adds them to the scutter task list.

Filters:
  * single-fetch-filter - avoids loops by remembering which resources have been fetched
  * shallow-depth-filter - limits crawl depth to 1 link deep
  * regex-filter - blocks all URLs containing the word "livejournal"


### mapping-scutter ###
A Scutter which simply discovers and maps connections between files using source/origin properties in its memory

Memory: a file-based memory named memory.rdf
Workers: 10 threads

Consumers:
  * rdf-consumer - looks for rdfs:seeAlso links in scuttered content and adds them to the scutter task list.

Filters:
  * single-fetch-filter - avoids loops by remembering which resources have been fetched
  * deep-depth-filter - limits crawl depth to 5 link deep

### persistent-scutter ###
A Scutter that includes writing incoming data into a persistent memory. Note that the memory is different to that holdings Scutter persistent state.

Memory: a file-based memory named memory.rdf
Workers: 10 threads

Consumers:
  * storer - stores HTTP responses in c:\temp\slug-cache
  * rdf-consumer - looks for rdfs:seeAlso links in scuttered content and adds them to the scutter task list.
  * persistent-storer - stores HTTP responses in a Jena persistent model

Filters:
  * single-fetch-filter - avoids loops by remembering which resources have been fetched
  * depth-filter - limits crawl depth to 3 links deep
  * regex-filter - blocks all URLs containing the word "livejournal"

### cache-builder ###

Builds a local cache of fetched data, doesn't traverse RDF links to discover new resources.

Memory: a file-based memory named memory.rdf
Workers: 10 threads

Consumers:
  * storer - stores HTTP responses in c:\temp\slug-cache