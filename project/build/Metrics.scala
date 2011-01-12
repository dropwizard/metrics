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
   * Publish via Ivy.
   */

  lazy val publishTo = Resolver.sftp("repo.codahale.com",
                                     "codahale.com",
                                     "/home/codahale/repo.codahale.com/")

  /**
   * Repositories
   */
  val dropWizard = "Coda's Repo" at "http://repo.codahale.com"

  /**
   * Dependencies
   */
  val jackson = "org.codehaus.jackson" % "jackson-core-asl" % "1.7.0"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5"

  /**
   * Test Dependencies
   */
  val simplespec = "com.codahale" %% "simplespec" % "0.2.0" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"
}
