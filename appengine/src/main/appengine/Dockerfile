FROM gcr.io/google_appengine/openjdk8
VOLUME /tmp
ADD gscribe-1.0-SNAPSHOT.jar gscribe.jar
ENTRYPOINT [ "java","-jar","/gscribe.jar","server","gscribe.yaml"]