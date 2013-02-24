Packages
=======================
* dispatcher-flush-refetch-samplecode-*.zip - This sample application will help you to configure your dispatcher to do re-fetching of files on flush.
  * To install it in your CQ instance:
    1. (In the browser) Go to /crx/packmgr/index.jsp and login as admin
    2. Install the package
    3. Go to your dispatcher flush agent configuration.  For example /etc/replication/agents.publish/flush.html
    4. Click Edit
    5. Set the following
       * Serialization Type = Re-fetch Dispatcher Flush
       * Extended => HTTP Method = POST
    6. Save

* dispatcher-webinar-samplecode-*.zip - This sample application includes the following:
  * CQ components that show an example of using Ajax and SSI to load component content.
  * The url filter app from https://github.com/cqsupport/cq-urlfilter app and a component demonstrating its use.


