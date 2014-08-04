Karibu-core
===========

Karibu core components for a Karibu based infrastructure: client-side
library, server side daemon, and the central serialization interfaces.

If you just want to use Karibu and not tingle with the inner workings,
you should find the **Karibu-tutorial** instead on GitHub, and go over
the tutorials and guides there. 

All the modules mentioned below more easily access through maven/ivy
dependency management, than by building them from the source
code. Review the Ant scripts in the **Karibu-tutorial** project.

Modules in Karibu-core
---

The following modules are contained in this project

  *  Karibu-serialization: The central `Serialization` and
     `Deserialization` interfaces that you have to implement for
     adapting Karibu for your specific type of data collection.

  * Karibu-producer: The client-side library code.

  * Karibu-consumer: The daemon code.	 

  * Karibu-test: JUnit test cases for all three modules above, as well
    as the execution scripts for the daemon. Review the README file in
    the folder.



