#!/bin/bash

## Compile
# mvn clean compile

mvn clean package
## Run
# java -cp target/classes com.clothingstore.app.client.Client


java -jar target/clothingstore-0.0.1-SNAPSHOT-jar-with-dependencies.jar