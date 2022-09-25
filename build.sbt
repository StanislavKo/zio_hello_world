scalaVersion := "2.13.8"
organization := "com.hsd"
name         := "cvwebhook"

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio"            % "2.0.1",
  "dev.zio"       %% "zio-json"       % "0.3.0-RC11",
  "dev.zio"       %% "zio-config"     % "3.0.1",
  "dev.zio"       %% "zio-config-typesafe" % "3.0.1",
  "dev.zio"       %% "zio-config-magnolia" % "3.0.1",
  "dev.zio"       %% "zio-streams"    % "2.0.0",
  "dev.zio"       %% "zio-kafka"      % "2.0.0",
  "io.d11"        %% "zhttp"          % "2.0.0-RC10",
  "io.getquill"   %% "quill-zio"      % "4.3.0",
  "io.getquill"   %% "quill-jdbc-zio" % "4.3.0",
  "com.h2database" % "h2"             % "2.1.214",
  "org.postgresql" % "postgresql"     % "42.5.0",
  "org.scalatest" %% "scalatest"      % "3.2.13" % "test",
  "org.scalatest" %% "scalatest-flatspec" % "3.2.13" % "test",
  "org.apache.kafka" % "kafka_2.13" % "2.5.0" % "test",
  "org.apache.kafka" % "kafka-clients" % "2.5.0" % "test",
)

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test"          % "2.0.2" % Test,
  "dev.zio" %% "zio-test-sbt"      % "2.0.2" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.0.2" % Test
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

assembly / mainClass := Some("com.hsd.cv.webhooks.MainApp")
assembly / assemblyJarName := "webhook.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

enablePlugins(DockerPlugin)

//docker / dockerfile := {
//  val artifact: File = assembly.value
//  val artifactTargetPath = s"/app/${artifact.name}"
//
//  new Dockerfile {
//    from("openjdk:8-jre")
//    add(artifact, artifactTargetPath)
//    entryPoint("java", "-jar", artifactTargetPath)
//  }
//}
docker / dockerfile := NativeDockerfile(file(".") / "Dockerfile")
docker / imageNames := Seq(
  ImageName(s"${organization.value}/${name.value}:latest"),
)
