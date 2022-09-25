Scala 2.13.8

sbt 1.6.2

```
docker-compose up

sbt test

sbt run

sbt clean

sbt assembly

sbt docker
```

[![Scaling up](https://img.youtube.com/vi/sx0rD1__mOQ/0.jpg)](https://www.youtube.com/watch?v=sx0rD1__mOQ)

Change log:
- HTTP server. Returned webhooks contain id;
- HTTP server. Validation of created webhooks is added;
- Downgrade to scala 2.13.8, sbt 1.6.2
- Unit tests (functional + integration + ZIO/integration)
- Sonarqube is plugged in - it doesn't analyze scala codebase
- branch[BEE-6/dirty-read], Dirty read from H2 database (postgresql doesn't implement read-uncommitted)
