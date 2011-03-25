import sbt._
import maven._

class MetricsProject(info: ProjectInfo) extends DefaultProject(info)
                                                with MavenDependencies
                                                with IdeaProject {
  /**
   * Publish the source as well as the class files.
   */
  override def packageSrcJar = defaultJarPath("-sources.jar")
  val sourceArtifact = Artifact.sources(artifactID)
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

  /**
   * Publish via maven-sbt.
   */
  lazy val publishTo = Resolver.sftp("repo.codahale.com",
                                     "codahale.com",
                                     "/home/codahale/repo.codahale.com/")

  /**
   * Repositories
   */
  val dropWizard = "Coda's Repo" at "http://repo.codahale.com"

  /**
   * Optional Dependencies
   */
  val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.3" % "optional"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "optional"
  val jetty = "org.eclipse.jetty" % "jetty-server" % "7.3.1.v20110307" % "optional"
  val guice = "com.google.inject" % "guice" % "2.0" % "optional"

  /**
   * Test Dependencies
   */
  val simplespec = "com.codahale" %% "simplespec" % "0.2.0" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"
}
