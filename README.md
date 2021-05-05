### Compilation

```./compile.sh```

### Usage

Load data from worldbank

```./run.sh --dataload```

Top countries by population, GDP/PPP growth

```./run.sh --results```

The output of this command should look like this:

```
Top 10 countries by population growth from 2010 to 2011
+----------+----------------+-----------------+
|Country Id|    Country Name|Population Growth|
+----------+----------------+-----------------+
|        IN|           India|       16,007,559|
|        CN|           China|        6,425,000|
|        NG|         Nigeria|        4,301,874|
|        PK|        Pakistan|        3,915,951|
|        ID|       Indonesia|        3,281,991|
|        ET|        Ethiopia|        2,499,963|
|        US|   United States|        2,235,208|
|        CD|Congo, Dem. Rep.|        2,191,299|
|        BR|          Brazil|        1,800,899|
|        EG|Egypt, Arab Rep.|        1,768,015|
+----------+----------------+-----------------+

Top 3 countries by GDP/PPP growth with top 10 by population growth from 2010 to 2011
+----------+-------------+-----------------+
|Country Id| Country Name|   GDP/PPP Growth|
+----------+-------------+-----------------+
|        CN|        China|1,465,557,235,547|
|        US|United States|  550,528,377,000|
|        IN|        India|  389,048,573,432|
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
