# Public Transit Map Matching

This repo is for my bachelor thesis and project at the chair of algorithms and datastructures at the university of Freiburg.

The goal of this project is to automatically generate shape files for bus routes of a GTFS feeds and evaluate the quality of the generated shapes against a ground truth.

## Setup:

- osm file for Baden-Württemberg: [download](http://download.geofabrik.de/europe/germany/baden-wuerttemberg-latest.osm.pbf)
- GTFS Feed Stuttgart: [download](https://www.openvvs.de/dataset/e66f03e4-79f2-41d0-90f1-166ca609e491/resource/bfbb59c7-767c-4bca-bbb2-d8d32a3e0378/download/vvs_gtfs.zip)

Set the *app.gh.osm* property to your osm file path. This can be done in application.properties file or via env variables.
 
## Development
run `.\mvnw clean quarkus:dev` to start the quarkus backend. The API is available via localhost:8080

run `npm run hot` inside the frontend module to. You can visit the site via localhost:9000
Inside *frontend/src/Config.js* set *apiEndpoint* to http://localhost:8080


## Production
The frontend will is served by the quarkus backend.

Inside *frontend/src/Config.js* set *apiEndpoint* to an empty string and run `npm run prod` inside the frontend module. This compiles and copies all files inside the backend resource folder.

run `\.mvnw clean package` to compile the project


## Docker
compile project: `./mvnw clean package -Dmaven.test.skip=true`

### with docker-compose
bind volume in docker-compose.yaml

run `docker-compose build` and `docker-compose up -d`

### with DockerFile
`sudo docker build -t michael-fleig-project .`

`
sudo docker run -i --rm -p 8080:8080 --name <name> <name>`

make sure to set the env variables (see docker-compose.yaml)
 