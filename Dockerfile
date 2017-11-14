FROM clojure
WORKDIR /usr/src/app
RUN mkdir -p /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
EXPOSE 3000
CMD lein ring server
