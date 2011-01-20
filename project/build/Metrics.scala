import sbt._

class Metrics(info: ProjectInfo) extends DefaultProject(info)
                                         with posterous.Publish
                                         with maven.MavenDependencies
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
  val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.1" % "optional"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "optional"
  val jetty = "org.eclipse.jetty" % "jetty-server" % "7.2.2.v20101205" % "optional"

  /**
   * Test Dependencies
   */
  val simplespec = "com.codahale" %% "simplespec" % "0.2.0" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"
}
