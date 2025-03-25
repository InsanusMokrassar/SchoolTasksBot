FROM bellsoft/liberica-openjdk-alpine:19

USER 1005

VOLUME /config.json
VOLUME /db
VOLUME /static

ENTRYPOINT ["/gpm.server/bin/gpm.server", "/config.json"]

ADD ./build/distributions/gpm.server.tar /
