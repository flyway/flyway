addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

addSbtPlugin("com.googlecode.flyway" % "sbt-flyway" % IO.readLines(new File("target/classes/version.txt")).head)

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
