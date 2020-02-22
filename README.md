#
Package-tracker

## Install and run
You need to have java, git and maven installed to your 
development machine. Git is used for version control and
cloning, maven is used to build the project with SQLite 
driver.

Commands are run in shell. 

Clone:
* git clone git://github.com/devgranlund/package-tracker

Run with script (for convenience reasons):
* sh compile-and-run.sh

Run manually: 
* mvn clean package
* java -jar target/package-tracker-1.0-SNAPSHOT.jar

## TODO
* Implement rest of the db functionality.