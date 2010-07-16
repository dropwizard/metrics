class Metrics(info: sbt.ProjectInfo) extends sbt.DefaultProject(info) with posterous.Publish with rsync.RsyncPublishing {
  /**
   * Publish the source as well as the class files.
   */
  override def packageSrcJar= defaultJarPath("-sources.jar")
  val sourceArtifact = sbt.Artifact(artifactID, "src", "jar", Some("sources"), Nil, None)
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

  /**
   * Publish via rsync.
   */
  def rsyncRepo = "codahale.com:/home/codahale/repo.codahale.com"

  /**
   * Repositories
   */
  val scalaToolsSnapshots = "scala-tools.org Snapshots" at "http://scala-tools.org/repo-snapshots"

  /**
   * Dependencies
   */
  val scalaTest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test" withSources() intransitive()
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test" withSources()
}
