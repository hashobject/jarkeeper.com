FROM clojure
WORKDIR /usr/src/app
RUN mkdir -p /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
EXPOSE 3080
CMD ["Could not transfer artifact com.firebase:firebase-client-jvm:pom:2.5.2 from/to central (https://repo1.maven.org/maven2/): Host name 'repo1.maven.org' does not match the certificate subject provided by the peer (CN=repo.maven.apache.org, O="Sonatype, Inc", L=Fulton, ST=MD, C=US)"]
