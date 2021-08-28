# Backend for the website to work with OTRS 6

Internship project for IT departament of Innopolis University

## Prerequisites

In order to compile project & build jar executable, you first need to install:
1. Intellij IDEA
2. In idea install plugins:
    - Kotlin
    - Ktor
    
## Building

After clone, in root directory of project execute:

- Terminal: `gradlew build`

## Running

Make sure you have installed java on your system

Simply run `java -jar otrs_final.jar`

If you want to add specific jvm arguments, place them here `java [here] -jar otrs_final.jar`. Example:

- `java -Xms256m -Xmx512m -jar otrs_final.jar`

You can bypass specific port by running `java -jar otrs_final.jar -port=80`. Default port is `81`