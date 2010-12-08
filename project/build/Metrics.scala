import sbt._

class Metrics(info: ProjectInfo) extends DefaultProject(info)
                                         with posterous.Publish
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

  lazy val publishTo = Resolver.sftp("Personal Repo",
                                     "codahale.com",
                                     "/home/codahale/repo.codahale.com/") as ("codahale")
  override def managedStyle = ManagedStyle.Maven

  /**
   * Dependencies
   */
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test" withSources ()
  val simplespec = "com.codahale" %% "simplespec" % "0.2.0" % "test" withSources ()
  val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources() intransitive()
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test" withSources()
}
