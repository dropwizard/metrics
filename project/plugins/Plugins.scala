class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val t_repo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.3"
}
