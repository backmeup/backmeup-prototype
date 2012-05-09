# Welcome to the BackMeUp Prototype

The aim of the BackMeUp Prototype is to provide a scalable personal data backup platform.
The BackMeUp Prototype will allow users to create backups of their personal data that is scattered across
the Web (e.g. on social networks, Web mail services, cloud storage provides or media sharing sites). 

BackMeUp will feature a modular architecture in which new services can be supported through connector plugins.
In addition to backup, users will also be able to schedule more complex processing workflows, including e.g.
encryption or data normalization/format conversion tasks.

## Prerequisites

The BackMeUp Prototype is built with [Maven] (http://maven.apache.org/).
Instructions on how to set up Maven on your machine are [available here] 
(http://maven.apache.org/download.html#Installation_Instructions).

## Getting Started

1. Change to the project folder to which you cloned the project repository

2. Build parts of the project: `mvn clean package install`

3. Build plugins: 
```
cd plugins
mvn clean package install
```

4. Setup standard-plugins and built plugins (dropbox and skydrive currently):
```
cd org.backmeup.grizzly
mkdir autodeploy
cp ../standard-plugins/*.jar autodeploy
cp ../plugins/build/*.jar autodeploy
```

5. To start the grizzly-rest-server, change to folder org.backmeup.grizzly and type `mvn -P standard exec:exec`

6. To start the dummy-rest-server, change to folder org.backmeup.grizzly and type `mvn -P dummy exec:exec`

7. To start the rest server in debug mode, use the additional debug profile: `mvn -P standard,debug exec:exec` (the debug-port will be 1044)

8. All parts are eclipse-projects aswell. To add them to your workspace use the eclipse command `Add Existing Projects`

9. To use maven within eclipse, install the Maven Integration (m2e) Plugin (http://www.eclipse.org/m2e/)

## Further Information 

More technical and developer information is being made available in the
[Wiki] (https://github.com/backmeup/backmeup-prototype/wiki).

