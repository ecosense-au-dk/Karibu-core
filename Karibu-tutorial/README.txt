List of tasks to be executed
===========================

 * mvn exec:java -Ddaemon	Execute the Karibu daemon

 * mvn exec:java -Ddaemon -Dpf=(resource-root-folder)  Exec. store daemon, use properties in given folder

  * mvn exec:java -Dload -Dpf=(resource-root-folder) -Dmaxprsec=(integer) Executes the load generator with (integer) messages per second.


Experimental

  * mvn exec:exec -Ddaemon2 -Dpf=(resource-root-folder) -Dhostname=(ip addr)
