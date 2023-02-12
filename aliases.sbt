addCommandAlias("com", "compile; Test / compile")
addCommandAlias(
  "check",
  "scalafmtSbtCheck; scalafmtCheckAll; githubWorkflowCheck"
)
addCommandAlias(
  "fix",
  "scalafmtSbt; scalafmtAll; scalafixAll; githubWorkflowGenerate"
)
addCommandAlias("dep", "dependencyUpdates")
