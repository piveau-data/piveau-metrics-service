FROM openjdk:jre-alpine

# Application configuration - change these
# Details can be found in the README
ENV PORT 8083
ENV PIVEAU_PIPE_LOG_LEVEL INFO


# Don't change these unless you know what you're doing
ENV PIVEAU_PIPE_LOG_APPENDER PIPEFILE
ENV PIVEAU_PIPE_LOG_PATH logs/mqa-metrics-service.%d{yyyy-MM-dd}.log

ENV VERTICLE_FILE mqa-metric-service-0.1-fat.jar
ENV VERTICLE_HOME /usr/verticles


EXPOSE $PORT

RUN addgroup -S vertx && adduser -S -g vertx vertx

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/

RUN chown -R vertx $VERTICLE_HOME
RUN chmod -R g+w $VERTICLE_HOME

USER vertx

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -Xmx2048m -jar $VERTICLE_FILE"]
