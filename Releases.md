This page contains some notes on the expected functionality that will be included in future iterations of Slug. Bear in mind that development is a part-time activity, with a priority set by what problems/features I either find interesting or necessary for my own projects.

Nagging is a good way to get features implemented. If I know someone is using the code, that's good motivation.

# 0.3 #

  * Number of bug fixes and improvements
  * Allow configuration of the User-Agent header
  * Expose and capture more statistics while in-progress
  * Complete rework of how RDF is handled and parsed
  * Follow more than just rdfs:seeAlso links to find data, by allowing pluggable link discovery
  * Alter the Consumer components can now pass data downstream using Result context
  * Controller now deals with avoiding cycles in a crawl, removing need for a filter to do this
  * Improved logging

# Wishlist #

Slug isn't very well behaved. It needs to:

  * Support the Robot Exclusion Protocol
  * Implement throttling on a global and per-domain basis
  * Check additional HTTP status codes to "skip" more errors
  * Support white-listing of URLs
  * Support Content Negotiation to allow for additional RDF syntaxes
  * Integrate a smushing utility for managing persistent data
  * Ability to retry urls
  * Support for RDFa and other content manipulation