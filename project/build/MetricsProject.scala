import sbt._
import maven._

class MetricsProject(info: ProjectInfo) extends ParentProject(info) with IdeaProject with MavenDependencies {
  lazy val publishTo = Resolver.sftp("repo.codahale.com", "codahale.com", "/home/codahale/repo.codahale.com/")

  lazy val core = project("core", "metrics-core", new CoreProject(_))

  lazy val jetty = project("jetty", "metrics-jetty", new JettyProject(_), core)

  lazy val guice = project("guice", "metrics-guice", new GuiceProject(_), core)

  lazy val servlet = project("servlet", "metrics-servlet", new ServletProject(_), core)

  lazy val log4j = project("log4j", "metrics-log4j", new Log4JProject(_), core)

  lazy val logback = project("logback", "metrics-logback", new LogbackProject(_), core)

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) with MavenDependencies with IdeaProject {
    lazy val publishTo = Resolver.sftp("repo.codahale.com", "codahale.com", "/home/codahale/repo.codahale.com/")

    /**
     * Publish the source as well as the class files.
     */
    override def packageSrcJar = defaultJarPath("-sources.jar")
    lazy val sourceArtifact = Artifact.sources(artifactID)
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

    /**
     * Repositories
     */
    val dropWizard = "Coda's Repo" at "http://repo.codahale.com"

    /**
     * Test Dependencies
     */
    val simplespec = "com.codahale" % "simplespec_2.8.1" % "0.2.0" % "test"
    val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"
  }

  class JettyProject(info: ProjectInfo) extends CoreProject(info) {
    val jetty = "org.eclipse.jetty" % "jetty-server" % "7.4.0.v20110414"
  }

  class GuiceProject(info: ProjectInfo) extends CoreProject(info) {
    val guice = "com.google.inject" % "guice" % "3.0"
  }

  class ServletProject(info: ProjectInfo) extends CoreProject(info) {
    val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.5"
    val servletApi = "javax.servlet" % "servlet-api" % "2.5"

    val jetty = "org.eclipse.jetty" % "jetty-servlet" % "7.4.0.v20110414" % "test"
  }

  class Log4JProject(info: ProjectInfo) extends CoreProject(info) {
    val log4j = "log4j" % "log4j" % "1.2.16"
  }

  class LogbackProject(info: ProjectInfo) extends CoreProject(info) {
    val logbackCore = "ch.qos.logback" % "logback-core" % "0.9.28" % "compile"
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "0.9.28" % "compile"

    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1" % "test"
  }
}
