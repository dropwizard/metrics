class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val t_repo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.4"

  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"

  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val mavenSBT = "com.codahale" % "maven-sbt" % "0.0.1"
}
