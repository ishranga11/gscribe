FROM gcr.io/google_appengine/openjdk8
VOLUME /tmp
ADD ./target/gscribe-1.0-SNAPSHOT.jar gscribe.jar
ADD src/main/resources/gscribe.yaml gscribe.yaml
ENTRYPOINT [ "java","-jar","/gscribe.jar","server","gscribe.yaml"]