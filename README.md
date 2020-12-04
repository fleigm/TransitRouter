# Public Transit Map Matching

This repo is for my bachelor thesis and project at the chair of algorithms and datastructures at the university of Freiburg.

The goal of this project is to automatically generate shape files for bus routes of a GTFS feeds and evaluate the quality of the generated shapes against a ground truth.

## Setup:

- osm file for Baden-Württemberg: [download](http://download.geofabrik.de/europe/germany/baden-wuerttemberg-latest.osm.pbf)
- GTFS Feed Stuttgart: [download](https://www.openvvs.de/dataset/e66f03e4-79f2-41d0-90f1-166ca609e491/resource/bfbb59c7-767c-4bca-bbb2-d8d32a3e0378/download/vvs_gtfs.zip)

 Set the correct paths in **backend/src/main/resources/application.properties**
 
## Development
run `npm run watch` inside the frontend module. The compiled code is copied into the backend resource folder automatically.

run `.\mvnw quarkus:dev` in the root folder

## Docker

compile project: `./mvnw clean package -Dmaven.test.skip=true`

### with docker-compose
bind volume in docker-compose.yaml

run `docker-compose build` and `docker-compose up -d`

### with DockerFile
`sudo docker build -t michael-fleig-project .`

`
sudo docker run -i --rm -p 8080:8080 --name <name> <name>`
 