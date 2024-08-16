addCommandAlias("com", "compile; Test / compile")
addCommandAlias("fmtcheck", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("dep", "dependencyUpdates")
addCommandAlias("site", "site/runMain ru.scalabook.site.app.GenerateSite")
addCommandAlias(
  "prePush",
  "clean; com; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
)
addCommandAlias(
  "fullCheck",
  "com; fmtcheck; test; undeclaredCompileDependencies; unusedCompileDependencies"
)
