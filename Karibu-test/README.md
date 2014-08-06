Karibu testing
==============

This module contains all the test-driven JUnit tests that have been developed
to create Karibu. It also contains an example domain and its
associated serializers and deserializers.

In addition, it contains a manual test, that replicate that from the
Karibu-tutorial. The main difference is that Maven is used for
execution. I find Maven rather tedious to use for execution but it is
provided here especially to execute the daemon during new development
of the core modules.




How to execute daemon and load generator
-----------

First - ensure you have executed `mvn install` in the root of
Karibu-core to make sure your local maven repository contains the
latest versions of the Karibu-core modules.

Next, to execute the Karibu daemon

  `mvn exec:exec -Ddaemon -Dpf=(resource-root-folder) -Dhostname=(ip addr of local machine)`

  example: mvn exec:exec -Ddaemon -Dpf=resource/lab-punda -Dhostname=10.11.82.60

The hostname is important to allow 'jconsole' to attach to the daemons
JMX port at 4672. However one issue is pending - as hitting Ctrl-c
does not really stop the daemon on Windows 7, you have to use the task
manager to do that :(


  `mvn exec:java -Dload -Dpf=(resource-root-folder) -Dmaxprsec=(integer)`

Executes the load generator with (integer) messages of type EXMRE001 per second.


Advice
---

The present module only contains the example domain serializer and
deserializer. To develop a production system, I strongly advice to
copy the Karibu-tutorial project instead and use that as basis for
developing your own data collection system. Ant is much better suited
for executing the daemon and developing your particular set of
serializers and deserializers.

