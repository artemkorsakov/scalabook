addCommandAlias("com", "compile; Test / compile")
addCommandAlias("check", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("dep", "dependencyUpdates")
addCommandAlias("site", "site/runMain ru.scalabook.site.app.GenerateSite")
addCommandAlias(
  "prepush",
  "com; fix; test; undeclaredCompileDependencies; unusedCompileDependencies"
)
addCommandAlias(
  "fullCheck",
  "com; check; test; undeclaredCompileDependencies; unusedCompileDependencies"
)
