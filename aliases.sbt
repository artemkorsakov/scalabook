addCommandAlias("com", "compile; Test / compile")
addCommandAlias("fmtcheck", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("dep", "dependencyUpdates")
addCommandAlias("site", "site/runMain ru.scalabook.site.app.GenerateSite")
addCommandAlias(
  "checkLinks",
  "site/runMain ru.scalabook.site.app.CheckLinksSite"
)
addCommandAlias(
  "prePush",
  "clean; com; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
)
addCommandAlias(
  "fullCheck",
  "clean; com; test; fmtcheck; undeclaredCompileDependencies; unusedCompileDependencies"
)
