# MQA metric service

Computes various metrics for the MQA and stores the results in a database.

## Setup

1. Install all of the following software
        
* Java JDK >= 1.8
* Git >= 2.17
* MongoDB >= 4.0.3
  
2. Clone the directory and enter it
    
        git clone git@gitlab.com:european-data-portal/mqa-metric-service.git
        
3. Edit the environment variables in the `Dockerfile` to your liking. Variables and their purpose are listed below:
   
| Key | Description | Default |
| :--- | :--- | :--- |
| PORT | Port this service will run on | 8080 |
| MONGODB_SERVER_HOST | MongoDB server address | localhost |
| MONGODB_SERVER_PORT | MongoDB port | 27017 |
| MONGODB_USERNAME | MongoDB user name | null |
| MONGODB_PASSWORD | MongoDB password | |
| MONGODB_DB_NAME | MongoDB database name | mqa-metrics |
| PGSQL_SERVER_HOST | PostgreSQL server address, including port and database name | jdbc:postgresql://localhost:5432/mqa |
| PGSQL_USERNAME | PostgreSQL user name | postgres | 
| PGSQL_PASSWORD | PostgreSQL password | postgres |
| PGSQL_DEFAULT_LIMIT | Default number of results to return | 20 |
| PIVEAU_PIPE_LOG_LEVEL | Log level | INFO |

        
## Run

### Production

Build the project by using the provided Maven wrapper. This ensures everyone this software is provided to can use the exact same version of the maven build tool.
The generated _fat-jar_ can then be found in the `target` directory.

* Linux
    
        ./mvnw clean package
        java -jar target/mqa-metric-service-0.1-fat.jar

* Windows

        mvnw.cmd clean package
        java -jar target/mqa-metric-service-0.1-fat.jar
      
* Docker

    1. Start your docker daemon 
    2. Build the application as described in Windows or Linux
    3. Adjust the port number (`EXPOSE` in the `Dockerfile`)
    4. Build the image: `docker build -t edp/mqa-metric-service .`
    5. Run the image, adjusting the port number as set in step _iii_: `docker run -i -p 8123:8123 edp/mqa-metric-service`
    6. Configuration can be changed without rebuilding the image by overriding variables: `-e PORT=8124`

### Development

For use in development two scripts are provided in the project's root folder. These enable hot deployment (dynamic recompiling when changes are made to the source code).
Linux users should run the `redeploy.sh` and Windows users the `redeploy.bat` file.

_Note_: The files generated by [VertX Codegen]([https://github.com/vert-x3/vertx-codegen]) may not be detected by your IDE. 
In this case, mark the directory `src/main/generated` as `Generated Sources Root`.

## CI

The repository uses the gitlab in-build CI Framework. The .gitlab-ci.yaml file starts as soon a new push event occurs. After running the test cases the application is build, a new docker image is created and stored in the gitlab registry. 

## API

A formal OpenAPI 3 specification can be found in the `src/main/resources/webroot/openapi.yaml` file.
A visually more appealing version is available at `{url}:{port}` once the application has been started.

## MongoDB schema

All global metrics are stored in single collection. 
All metrics belonging to a certain catalogue are stored in a separate collection named after the catalogue's ID.
