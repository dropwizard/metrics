import sbt._
import maven._
import de.element34.sbteclipsify._

class MetricsProject(info: ProjectInfo) extends ParentProject(info) with IdeaProject with MavenDependencies {
  lazy val publishTo = Resolver.sftp("repo.codahale.com", "codahale.com", "/home/codahale/repo.codahale.com/")

  lazy val core = project("core", "metrics-core", new CoreProject(_))

  lazy val jetty = project("jetty", "metrics-jetty", new JettyProject(_), core)

  lazy val guice = project("guice", "metrics-guice", new GuiceProject(_), core)

  lazy val servlet = project("servlet", "metrics-servlet", new ServletProject(_), core)

  lazy val graphite = project("graphite", "metrics-graphite", new GraphiteProject(_), core)

  lazy val log4j = project("log4j", "metrics-log4j", new Log4JProject(_), core)

  lazy val logback = project("logback", "metrics-logback", new LogbackProject(_), core)

  lazy val ehcache = project("ehcache", "metrics-ehcache", new EhcacheProject(_), core)

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) with MavenDependencies with IdeaProject with Eclipsify {
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
    val simplespec = "com.codahale" %% "simplespec" % "0.3.3" % "test"
    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  }

  class JettyProject(info: ProjectInfo) extends CoreProject(info) {
    val jetty = "org.eclipse.jetty" % "jetty-server" % "7.4.0.v20110414"
  }

  class GuiceProject(info: ProjectInfo) extends CoreProject(info) {
    val guice = "com.google.inject" % "guice" % "3.0"
  }

  class GraphiteProject(info: ProjectInfo) extends CoreProject(info) {
    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1" % "compile"
  }

  class ServletProject(info: ProjectInfo) extends CoreProject(info) {
    val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.6"
    val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"

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

  class EhcacheProject(info: ProjectInfo) extends CoreProject(info) {
    val ehcache = "net.sf.ehcache" % "ehcache-core" % "2.4.2"
  }
}
