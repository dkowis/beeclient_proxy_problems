## Bee Client doesn't actually honor the proxy settings :(

I was trying to use the handy Betamax proxy to integration test my applications HTTP interactions, but sadly, and
contrary to the Bee Client documentation, it doesn't appear to automatically take advantage of the java VM
properties:

* http.proxyHost
* http.proxyPort
* https.proxyHost
* https.proxyPort

I have written some test cases, as well as a main client that can be executed as follows:

* Main client: `gradle withProxy`
* Test: `gradle test`
* Test: `gradle test -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=5555 -Dhttps.proxyPort=127.0.0.1 -Dhttps.proxyPort=5555`

The same problem evidences itself in all three executions, easier to see in the tests, the Bee Client does not honor the
JVM proxy settings and must be manually specified. :(