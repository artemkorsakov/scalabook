addCommandAlias("com", "compile; Test / compile")
addCommandAlias("fmtcheck", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("site", "site/runMain ru.scalabook.site.app.GenerateSite")
addCommandAlias("sitePdf", "site/runMain ru.scalabook.site.app.GeneratePdf")
addCommandAlias(
  "checkLinks",
  "site/runMain ru.scalabook.site.app.CheckLinksSite"
)
addCommandAlias(
  "prePush",
  "clean; com; testOnly; fix; undeclaredCompileDependencies; unusedCompileDependencies"
)
addCommandAlias(
  "fullCheck",
  "clean; com; testOnly; fmtcheck; undeclaredCompileDependencies; unusedCompileDependencies"
)
