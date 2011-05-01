import sbt._
import maven._

class MetricsProject(info: ProjectInfo) extends ParentProject(info) with IdeaProject with MavenDependencies {
  lazy val publishTo = Resolver.sftp("repo.codahale.com", "codahale.com", "/home/codahale/repo.codahale.com/")

  class MetricsModule(info: ProjectInfo) extends DefaultProject(info) with MavenDependencies with IdeaProject {
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

  lazy val core = project("core", "metrics-core")

  lazy val jetty = project("jetty", "metrics-jetty", new JettyProject(_), core)

  lazy val guice = project("guice", "metrics-guice", new GuiceProject(_), core)

  lazy val servlet = project("servlet", "metrics-servlet", new ServletProject(_), core)

  class JettyProject(info: ProjectInfo) extends MetricsModule(info) {
    val jetty = "org.eclipse.jetty" % "jetty-server" % "7.4.0.v20110414"
  }

  class GuiceProject(info: ProjectInfo) extends MetricsModule(info) {
    val guice = "com.google.inject" % "guice" % "3.0"
  }

  class ServletProject(info: ProjectInfo) extends MetricsModule(info) {
    val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.5"
    val servletApi = "javax.servlet" % "servlet-api" % "2.5"
  }
}
