#!/bin/bash
export WORLD_STATS_DB_URL=jdbc:h2:./h2/world-stats-db:statistics

java -jar ./target/scala-2.13/world-statsnge-assembly-1.0-SNAPSHOT.jar $@