class Metrics(info: sbt.ProjectInfo) extends sbt.DefaultProject(info) with posterous.Publish {
  /**
   * Home Repo
   */
  override def managedStyle = sbt.ManagedStyle.Maven
  val publishTo = sbt.Resolver.file("Local Cache", ("." / "target" / "repo").asFile)

  /**
   * Publish to a local temp repo, then rsync the files over to repo.codahale.com.
   */
  def publishToLocalRepoAction = super.publishAction
  override def publishAction = task {
    log.info("Uploading to repo.codahale.com")
    sbt.Process("rsync", "-avz" :: "target/repo/" :: "codahale.com:/home/codahale/repo.codahale.com" :: Nil) ! log
    None
  } describedAs("what") dependsOn(test, publishToLocalRepoAction)

  /**
   * Publish the source as well as the class files.
   */
  override def packageSrcJar= defaultJarPath("-sources.jar")
  val sourceArtifact = sbt.Artifact(artifactID, "src", "jar", Some("sources"), Nil, None)
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

  /**
   * Repositories
   */
  val scalaToolsSnapshots = "scala-tools.org Snapshots" at "http://scala-tools.org/repo-snapshots"

  /**
   * Dependencies
   */
  val scalaTest = "org.scalatest" % "scalatest" % "1.0.1-for-scala-2.8.0.Beta1-with-test-interfaces-0.3-SNAPSHOT" % "test" withSources()
  val mockito = "org.mockito" % "mockito-core" % "1.8.0" % "test" withSources()
}
