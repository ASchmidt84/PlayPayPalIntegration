name := "PayPalModule"

version := "1.0"

organization := "de.intelligyscience"

organizationName := "Intelligy Science UG (haftungsbeschränkt)"

scalaVersion := "2.11.7"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.2.0"


publishTo := Some(
  if (version.value endsWith "-SNAPSHOT")
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

licenses += (("MIT", url("http://opensource.org/licenses/MIT")))
startYear := Some(2016)

pomExtra :=
  <developers>
      <developer>
          <id>IntelligyScience</id>
          <name>Andre Schmidt</name>
          <url>https://github.com/IntelligyScience</url>
      </developer>
  </developers>

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

homepage := Some(url("https://github.com/IntelligyScience/PlayPayPalIntegration"))

scmInfo := Some(ScmInfo(
  url("https://github.com/IntelligyScience/PlayPayPalIntegration"),
  "scm:git:https://github.com/IntelligyScience/PlayPayPalIntegration.git",
  Some("scm:git:git@github.com:IntelligyScience/PlayPayPalIntegration.git")
))

credentials += Credentials(Path.userHome / ".sbt" / ".isCredentials")

publishTo := {
  val nexus = "http://46.4.103.179:8888/nexus/content/repositories/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "snapshots/")
  else
    Some("releases"  at nexus + "releases/")
}