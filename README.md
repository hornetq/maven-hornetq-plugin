# Maven plugin to start/stop HornetQ servers.

### Using the latest SNAPSHOT version

To use the latest SNAPSHOT version: clone this project and install its artifacts to your local Maven repository with `mvn install`.

### Using released versions

Released versions are available at the [JBoss Maven repository]. A simple [search for hornetq-maven-plugin] shows what is available. If you have not done this already, you will need to configure the repository path in your Maven `settings.xml`.

[search for hornetq-maven-plugin]: https://repository.jboss.org/nexus/index.html#nexus-search;quick~hornetq-maven-plugin
[JBoss Maven repository]: https://repository.jboss.org/nexus/index.html

## Examples

To run all examples

```mvn verify```

To run the client example which shows how to run a standalone java class as a client

```mvn verify -Pclient-example```

To run the example showing an integration test run

```mvn verify -Pit-example```
