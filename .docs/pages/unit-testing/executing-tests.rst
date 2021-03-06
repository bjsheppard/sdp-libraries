.. _Test Execution: 

--------------
Test Execution
--------------

For our SDP Pipeline Libraries, tests are placed in a directory called ``unit-tests``
within the library directory. 

===========================
Executing tests with Gradle
===========================

This repo has been set up to run Jenkins-Spock tests using Gradle.

From the root of the sdp-libraries repository, run ``make test``.

===========================
Executing tests with Docker
===========================

The unit tests can also be executed via Docker.  From the root 
of the sdp-libraries repository, run ``make test docker``. 

====================
Viewing Test Results
====================

Spock will create an HTML test report.  After running tests, 
you can run ``open target/reports/tests/test/index.html``. 
