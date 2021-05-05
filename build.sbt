scalaVersion := "2.13.5"

/// Dependencies

val akka       = "com.typesafe.akka" %% "akka-actor"  % "2.6.14"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.6.14"

val circeVersion = "0.12.3"
val circeCore    = "io.circe" %% "circe-core" % circeVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeParser  = "io.circe" %% "circe-parser" % circeVersion
val circeConfig  = "io.circe" %% "circe-config" % "0.7.0"

val macwire      = "com.softwaremill.macwire"   %% "macros"        % "2.3.7" % "provided"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"

val logbackClassic = "ch.qos.logback"       % "logback-classic"          % "1.2.3"
val logbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "6.5"

val scalatestVersion  = "3.2.7"
val scalatest         = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
val scalatestFreespec = "org.scalatest" %% "scalatest-freespec" % scalatestVersion

val slickVersion  = "3.3.3"
val slick         = "com.typesafe.slick" %% "slick" % slickVersion
val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
val slf4jNop      = "org.slf4j" % "slf4j-nop" % "1.6.4"
val h2            = "com.h2database" % "h2" % "1.4.200"

val sttpVersion         = "3.2.3"
val sttpAkkaHttpBackend = "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttpVersion
val sttpCirce           = "com.softwaremill.sttp.client3" %% "circe" % sttpVersion
val sttpCore            = "com.softwaremill.sttp.client3" %% "core" % sttpVersion

val typesafeConfig = "com.typesafe" % "config" % "1.4.1"

lazy val root = (project in file("."))
  .configs(ITest)
  .settings(inConfig(ITest)(Defaults.testSettings): _*)
  .settings(
    name := "worldstats",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
        akkaStream,
        circeCore,
        circeGeneric,
        circeParser,
        circeConfig,
        macwire,
        scalaLogging,
        logbackClassic,
        logbackEncoder,
        scalatest,
        slick,
        slickHikaricp,
        slf4jNop,
        h2,
        sttpAkkaHttpBackend,
        sttpCirce,
        sttpCore,
        typesafeConfig
      )
  )


lazy val ITest = config("it") extend (Test)

scalaSource in ITest := baseDirectory.value / "/src/it/scala"

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case x => MergeStrategy.first
}