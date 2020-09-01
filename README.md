### current issues:
- quarkus:dev does not parse arguments correctly -> see https://github.com/quarkusio/quarkus/pull/10170
- cannot run project from intellij -> see https://github.com/quarkusio/quarkus/issues/10147

until those are fixed use `java -jar target/web-1.0-SNAPSHOT-runner.jar` to start the project

---


The project is currently located in the _web_ subfolder.
In the future there will be two separate projects for the cli and the web application.

## build and run

`cd web`

### Compile project
`./mvnw clean package`

### Build docker container
`sudo docker build -f src/main/docker/Dockerfile.jvm -t michael-fleig-project .`

### Run project via docker
`
sudo docker run -i --rm -p 8080:8080 
-v ~/uni/bachelor/project/files:/var/ptmm 
--name michael-fleig-project
michael-fleig-project 
--osm-file="/var/ptmm/freiburg-regbez-latest.osm.pbf" 
--gtfs-file="/var/ptmm/VAGFR.zip"
`
### Run project via terminal
`java -jar target/web-1.0-SNAPSHOT-runner.jar`

### Options and commands

- generate <generated_gtfs_file>: creates a gtfs feed with the generated shapes
- web: starts a web application to view generated shapes on a map. The osm graph and gtfs feed will be loaded on the 
first request so this might take a while.
The web app can be reached via localhost:8080

- --osm-file: path to the osm file
- --gtfs-file: path to the gtfs file


### Build web app 
`cd web/src/webapp`

`npm run prod|dev|watch`