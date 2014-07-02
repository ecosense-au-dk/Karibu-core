List of tasks to be executed
===========================

Execute the Karibu daemon

  * mvn exec:exec -Ddaemon -Dpf=(resource-root-folder) -Dhostname=(ip addr of local machine)

  example: mvn exec:exec -Ddaemon -Dpf=resource/lab-punda -Dhostname=10.11.82.60

  The hostname is important to allow 'jconsole' to attach to the daemons JMX
  port at 4672. However one issue is pending - as hitting Ctrl-c does not
  really stop the daemon, you have to use the task manager to do that :(


  * mvn exec:java -Dload -Dpf=(resource-root-folder) -Dmaxprsec=(integer) 

    Executes the load generator with (integer) messages per second.




