#!/bin/sh
java -Djetty.host=127.0.0.1 -Djetty.port=8073 -jar ../webapps/jetty-runner.jar --port 8073 ../webapps/role-uu-prototype --path /role ../

