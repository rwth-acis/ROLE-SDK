#!/bin/bash

# changing working directory to script-location
cd "$(dirname "$0")"

java -Djetty.host=127.0.0.1 -Djetty.port=8073 -jar ../webapps/jetty-runner.jar --port 8073 ../webapps/role-uu-prototype --path /role ../
