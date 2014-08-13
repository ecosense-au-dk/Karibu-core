Karibu-core
===========

Version 1, August 2014.

*Henrik Bærbak Christensen, Department of Computer Science, University
 of Aarhus.*


Karibu core components for a Karibu based infrastructure: client-side
library, server side daemon, and the central serialization interfaces.

*Note:* You should normally not clone this repository unless you want
 to fix defects or propose new features. The contents of these modules
 are more easily fetched using maven or ivy. To *use* Karibu, please
 go to the
 [karibu-tutorial](https://github.com/ecosense-au-dk/karibu-tutorial)
 instead on GitHub, and go over the tutorials and guides there.

Modules in Karibu-core
---

The following modules are contained in this project

  *  Karibu-serialization: The central `Serialization` and
     `Deserialization` interfaces that you have to implement for
     adapting Karibu for your specific type of data collection.

  * Karibu-producer: The client-side library code.

  * Karibu-consumer: The daemon code.	 

  * Karibu-test: JUnit test cases for all three modules above, as well
    as an example execution scripts for the daemon. Review the README file in
    the folder.

Building
---

To build the core modules, you will need Java JDK 1.7.+ and Maven 3.2.+.

From the root you just do the standard `mvn install`.

Learning Karibu-core
---

If you want to contribute to the core Karibu modules, I advice you
first learn how to use Karibu, following the
[Karibu-tutorial](https://github.com/ecosense-au-dk/karibu-tutorial). Next,
the module `karibu-test` contains a lot of JUnit test that you may use
to get into the details of Karibu core.

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

Releasing and deploying Karibu modules
---

Generally, the release and branching strategy used in Karibu is taken
from [A successful Git branching
model](http://nvie.com/posts/a-successful-git-branching-model/).

The release and deployment procedure is outlined in detail in the
internal AU document in the project 'Karibu-EcoSense-Production'.

Contact Henrik Bærbak if you are interested.




