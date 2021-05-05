### Compilation

```./compile.sh```

### Usage

Load data from worldbank

```./run.sh --dataload```

Top countries by population, GDP/PPP growth

```./run.sh --results```

The output of this command should look like this:

```
Top 10 countries by population growth from 2010 to 2018
+----------+----------------+-----------------+
|Country Id|    Country Name|Population Growth|
+----------+----------------+-----------------+
|        IN|           India|      118,336,158|
|        CN|           China|       55,025,000|
|        NG|         Nigeria|       37,371,543|
|        PK|        Pakistan|       32,790,389|
|        ID|       Indonesia|       25,829,220|
|        ET|        Ethiopia|       21,584,595|
|        CD|Congo, Dem. Rep.|       19,504,237|
|        US|   United States|       17,365,835|
|        EG|Egypt, Arab Rep.|       15,662,360|
|        BD|      Bangladesh|       13,780,609|
+----------+----------------+-----------------+

Top 3 countries by GDP/PPP growth with top 10 by population growth from 2010 to 2018
+----------+-------------+-----------------+
|Country Id| Country Name|   GDP/PPP Growth|
+----------+-------------+-----------------+
|        CN|        China|9,367,703,435,128|
|        US|United States|5,588,107,049,000|
|        IN|        India|3,772,429,178,507|
+----------+-------------+-----------------+
```


### Notes
The app loads data (population, GDP/PPP by country/year) for given time range from worldbank and stores it into H2 database for further analysis.

Data is persisted in ./h2 directory, so it can be reused on next app run.

Time range can be configured in `./src/main/resources/application.conf`

Project contains some unit/integration tests, not all code is covered due to time restriction.

Integration tests are located under `src/it` directory, so (fast) unit tests can be run often during local development without running potentially slow / complex integration tests.


sbt executable is included so no need to install it separately.

### Libraries used:
- [sttp](https://sttp.softwaremill.com/en/latest/) for remote api calls
- [slick](https://scala-slick.org/docs/) with H2 for persistence
- [circe](https://circe.github.io/circe/) for json
- macwire for DI
