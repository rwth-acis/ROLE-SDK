ROLE-SDK
========

To build the SDK run mvn clean package from the main folder.

The result is a zip file (there is also a .tar.gz version).
path_to_role_svn_trunk/assembly/target/role-m<x>-sdk.zip

Running the SDK:
1) Unpackage the zip file
2) Follow further instructions in the unpackaged README

## Run with Docker

First build the image:

```bash
docker build -t role-sdk . 
```

Then you can run the image with

```bash
docker run -p 8073:8073 role-sdk
```

The ROLE-SDK will be available at http://127.0.0.1:8073/.

You can customize the start command by setting further arguments behind above command.
The following arguments are the the default arguments:

```bash
docker run -p 8073:8073 role-sdk java -Djetty.host=127.0.0.1 -Djetty.port=8073 -jar webapps/jetty-runner.jar --port 8073 webapps/role-uu-prototype --path /role .
```