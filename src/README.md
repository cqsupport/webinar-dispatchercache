Sample Code
----
* Components under /apps/samplecode/components
  * ajax-example - Shows how to use ajax to request a single component resource's content.
  * ssi-example - Shows how to use SSI to request a single component resource's content.
  * ajax-and-ssi-example - Shows how to use ajax in a component when accessing CQ directly and use SSI when going through dispatcher.
  * url-filter-test - Demonstrates how the cq-urlfilter works.  See [here](https://github.com/cqsupport/cq-urlfilter) for the implementation of the filter.

* /apps/sling/servlet/errorhandler/404.jsp and default.jsp - These are used for disabling the authentication handling for login redirection that happens on 404 not found error in a default CQ publish install.

* refetching-flush-agent - sample dispatcher flush serialization type that causes the dispatcher to re-fetch certain files instead of deleting them.
  * This code was written originally by Dominique Pfister from Day/Adobe.
  * This implementation works like this:
    * If the flush path has no file extension then {flush-path}.html will be re-fetched.
    * Otherwise the unaltered {flush-path} will be re-fetched (this handles the case of static files and dam:Assets)
  * Depending on how you serve content in your site you may want to modify this to handle re-fetching differently.
