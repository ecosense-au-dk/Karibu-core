List of tasks to be executed
===========================

Execute the Karibu daemon

  * mvn exec:exec -Ddaemon2 -Dpf=(resource-root-folder) -Dhostname=(ip addr of local machine)

  example: mvn exec:exec -Ddaemon2 -Dpf=resource/lab-punda -Dhostname=10.11.82.60

  The hostname is important to allow 'jconsole' to attach to the daemons JMX
  port at 4672. One issue pending is that the process does not stop
  if you hit ctrl-c



 * mvn exec:java -Ddaemon	Execute the Karibu daemon

 * mvn exec:java -Ddaemon -Dpf=(resource-root-folder)  Exec. store daemon, use properties in given folder

  * mvn exec:java -Dload -Dpf=(resource-root-folder) -Dmaxprsec=(integer) Executes the load generator with (integer) messages per second.




