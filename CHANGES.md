Changes to Karibu-core
======================

Release 2.1.0
-------------

 *) Fixed defect in daemon that made unfinite loop and flooding with
    connections. Now, if a message is tried to be stored in MongoDB whose
    size is over the 16 MB document size, the message is simply lost;
    not requeued for failing once more!

Release 2.0.3
-------------

 *) Statistics handler defects corrected, the DNS name is recorded and
    debug output removed from daemon.


Release 2.0.2
-------------

 *) No functional changes

Release 2.0.0
-------------

 *) Initial release, code ported from Ecosense production system.