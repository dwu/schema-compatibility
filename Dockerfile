FROM eclipse-temurin:11-jre

ENV TESTS /app/tests
ENV REPORTFILE /app/tests/report.html
ENV JARFILE schema-compatibility-1.0-SNAPSHOT-jar-with-dependencies.jar

RUN curl -sL https://deb.nodesource.com/setup_18.x | bash - \
  && curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - \
  && echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list \
  && apt-get update -qq \
  && apt-get install -qq --no-install-recommends \
    nodejs \
    yarn \
  && apt-get upgrade -qq \
  && rm -rf /var/lib/apt/lists/*

ADD target/${JARFILE} /app/
ADD report-builder/ /app/report-builder/

RUN cd /app/report-builder && npm install

WORKDIR /app
CMD ["node", "report-builder/app.js", "-d", "${TESTS}"]
