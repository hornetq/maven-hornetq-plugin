This is a maven plugin that allows you to run hornetq servers via maven.

At the moment this is not available in any repository so if you want to ue it or run the example you need to install it
locally to your maven repository by running 'mvn install'.

There are 2 examples shipped, to run these you need to cd into the example directory.

To run the client example which shows how to run a standalone java class as a client simply

'mvn verify -Pclient-example'

To run the example showing an integration test run

'mvn verify -Pit-example'

or to run both

'mvn verify -Pall'