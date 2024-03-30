addCommandAlias("com", "compile; Test / compile")
addCommandAlias("fmtcheck", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("dep", "dependencyUpdates")
addCommandAlias("site", "site/runMain ru.scalabook.site.app.GenerateSite")
addCommandAlias(
  "prepush",
  "com; fix; test; undeclaredCompileDependencies; unusedCompileDependencies"
)
addCommandAlias(
  "fullcheck",
  "com; fmtcheck; test; undeclaredCompileDependencies; unusedCompileDependencies"
)
