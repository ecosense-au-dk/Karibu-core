Karibu-core
===========

Karibu core components for a Karibu based infrastructure: client-side
library, server side daemon, and the central serialization interfaces.

If you just want to use Karibu and not tingle with the inner workings,
you should find the *Karibu-tutorial* instead on GitHub, and go over
the tutorials and guides there. 

All the modules mentioned below more easily access through maven/ivy
dependency management, than by building them from the source
code. Review the Ant scripts in the *Karibu-tutorial* project.

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

Building
---

To build the core modules, you will need Java JDK 1.7.+ and Maven 3.2.+.

From the root you just do the standard `mvn install`.

Eclipse
---

This always seems to be tricky but here is the procedure I normally use

 * Use `mvn eclipse:eclipse` (and perhaps a `mvn eclipse:clean` before
   that to ensure a clean start).

 * Make an Eclipse working set, like 'karibu-core'

 * Right-click it and select Import; in the menu, select General and
   subitem 'Existing Projects into Workspace'. Hit 'Next'.

 * In the dialog, select the karibu-core directory. Click 'Browse' and
   select all the projects that appear in the menu. Click 'Finish'.

 * The first time around, you have to add the classpath variable
   'M2_REPO'. Under Eclipse general preferences, browse through
   'Java', 'Build Path', and 'Classpath Variables'. Next, click on
   'New' and type 'M2_REPO' in the field 'Name', and write the path to
   your local Maven repository (e.g. ~/.m2/repository or
   %USERPROFILE%\.m2\repository) in the field 'Path'. Finish with
   'OK'.

Deployment at Computer Science, Aarhus University
---

(This is for internal reference).

You will need to have proper credentials installed in your _settings.xml_
in your M2_REPO folder.

For test deployments you can deploy to _twiga-test_, for production
deployment you can deploy to the public Computer Science twiga
Artifactory repository server. You will have to edit the pom.xml in
the _distributionManagement_ tag.

The command is `mvn deploy`.



