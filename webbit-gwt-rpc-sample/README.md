This is a sample project using the webbit-gwt library. To build it, run `mvn install`.
To run it, launch the resulting jar in java:

    > mvn install
    > java -jar target/target/webbit-gwt-sample-0.0.1-SNAPSHOT.jar
    Chat room running on: http://mycomputer.local:9876/

Any computer should be able to connect to this server now and chat. Each user should
see every other user's messages, and should be able to see each other user join and
leave.