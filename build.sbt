ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-weaver-global-resource-repro",
    libraryDependencies += "org.typelevel" %% "weaver-cats" % "0.13.1" % Test,
    Test / fork := true,
  )

// Workaround (uncomment to make the tests pass): run all tests in one forked JVM
// Test / testGrouping := Def.uncached {
//   Seq(Tests.Group("all", (Test / definedTests).value, Tests.SubProcess((Test / forkOptions).value)))
// }
