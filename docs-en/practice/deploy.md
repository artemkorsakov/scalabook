# Creating a New Scala 3 Project

So, let's start by setting up a new project.

## Prerequisites

[**Install Scala and sbt**](https://docs.scala-lang.org/ru/getting-started/install-scala.html): 
SBT (Scala Build Tool) is the standard tool for building Scala projects. 
It manages dependencies, compilation, and running your code.

## Step 1: Generating a Project Template

The fastest way to start a new project is to use a ready-made template.

1. Open a terminal in the directory where you want to create the project.
2. Run the following command:
    ```bash
    sbt new scala/scala3.g8
    ```
    This command will do the following:
    - `sbt new` — an SBT command for creating a project from a template (Giter8).
    - `scala/scala3.g8` — points to the official template for Scala 3.
    - SBT will ask for your project name (e.g., `PersonalDevelopmentPlan`). 
      After entering, a folder with this name will be created, containing the basic project structure.

**Useful Links:**

- [Building files with sbt](https://docs.scala-lang.org/ru/scala3/book/tools-sbt.html) — a general guide to working with SBT.
- [What is sbt new](https://www.scala-sbt.org/1.x/docs/sbt-new-and-Templates.html) — documentation on templates.

## Step 2: Connecting Useful SBT Plugins

Plugins extend SBT's functionality, automating routine tasks and helping maintain code quality.
We will add six important plugins.

1. In your project folder, create a file at `project/plugins.sbt`.
    If the `project` folder already exists, simply create the file inside it.
2. Add the following lines to this file:
    ```dotty
    addSbtPlugin("ch.epfl.scala"           % "sbt-scalafix"              % "0.14.8")
    addSbtPlugin("com.github.cb372"        % "sbt-explicit-dependencies" % "0.3.1")
    addSbtPlugin("com.github.reibitto"     % "sbt-welcome"               % "0.6.0")
    addSbtPlugin("com.github.sbt"          % "sbt-native-packager"       % "1.11.7")
    addSbtPlugin("org.jmotor.sbt"          % "sbt-dependency-updates"    % "1.2.9")
    addSbtPlugin("org.scalameta"           % "sbt-scalafmt"              % "2.6.1")
    ```

### Purpose of Each Plugin

#### sbt-scalafix Static Code Analysis

- **[Repository](https://github.com/scalacenter/sbt-scalafix)**
- **[Rule descriptions](https://scalacenter.github.io/scalafix/docs/users/installation.html)**
- **What does it do?** Performs static code analysis and applies refactoring rules.
  Helps find and fix issues, deprecated constructs, and bring code to a unified style.
  Especially effective in large teams, helping to ensure a unified code style.
- **Setup:** Create a `.scalafix.conf` file in the project root with the following content:

  ```text
  # Enable the set of rules
  rules = [
      DisableSyntax         # Disallows the use of some "dangerous" constructs
      LeakingImplicitClassVal
      OrganizeImports       # Removes unused imports
      RedundantSyntax       # Removes redundant syntax
  ]

  # Specific settings for the DisableSyntax rule
  DisableSyntax.noVars = true          # Disallows `var` - mutable variables
  DisableSyntax.noThrows = true        # Disallows `throw` - exceptions
  DisableSyntax.noNulls = true         # Disallows `null`
  DisableSyntax.noReturns = true       # Disallows the `return` keyword
  DisableSyntax.noWhileLoops = true    # Disallows `while` loops
  DisableSyntax.noAsInstanceOf = true  # Disallows unsafe type casts to AnyRef
  DisableSyntax.noIsInstanceOf = true  # Disallows unsafe isInstanceOf checks
  DisableSyntax.noXml = true           # Disallows deprecated XML literals in Scala
  DisableSyntax.noDefaultArgs = true   # Disallows default arguments in methods
  DisableSyntax.noFinalVal = true      # Disallows `final val`
  DisableSyntax.noFinalize = true      # Disallows overriding the `finalize` method
  
  # Rules for standardizing imports
  OrganizeImports {
      removeUnused = false
      targetDialect = Scala3
    
      groupedImports = Keep
      coalesceToWildcardImportThreshold = 5
  }
  ```

#### sbt-scalafmt Formatting

- **[Repository](https://github.com/scalameta/scalafmt/)**
- **What does it do?** Automatically formats your source code according to given rules.
  Ensures that all code in the project looks the same, making it easier to read and collaborate.
- **Setup:** Create a `.scalafmt.conf` file in the project root with the following content:

  ```text
  # scalafmt version
  version = "3.11.5"

  # Alignment style
  align.preset = more

  # Scala dialect used
  runner.dialect = scala3

  # Line wrapping settings
  newlines.source = keep
  # Automatically converts Scala 2 syntax to Scala 3 (where possible)
  rewrite.scala3.convertToNewSyntax = true
  # Adds "end" markers for constructs (e.g., `end if`) after 20 lines
  rewrite.scala3.insertEndMarkerMinLines = 20
  ```

#### sbt-dependency-updates Dependency Updates

- **[Repository](https://github.com/aiyanbo/sbt-dependency-updates)**
- **What does it do?** Upon an sbt command, checks Maven/Ivy repositories and shows a list of dependencies
  and SBT plugins themselves for which new versions are available. Helps keep the project up to date.
- **Usage:** Run the `dependencyUpdates` command in SBT.

#### sbt-explicit-dependencies Hidden Dependencies

- **[Repository](https://github.com/cb372/sbt-explicit-dependencies)**
- **What does it do?** Checks that all libraries used by your code for compilation are explicitly declared in `build.sbt`.
  Sometimes SBT can implicitly pull in dependencies, leading to "hidden" dependencies and issues when they change.
  This plugin eliminates such implicitness.
- **Usage:** Run the `undeclaredCompileDependencies` and `unusedCompileDependencies` commands in SBT.

#### sbt-welcome Welcome Message

- **[Repository](https://github.com/reibitto/sbt-welcome)**
- **What does it do?** Shows a customizable welcome message every time SBT starts.
  This can be useful for reminding about useful commands, documentation links, or
  displaying project ASCII art. Makes working with the SBT console more friendly and informative.

  ```text
  Welcome to
  
    _____    _____    _____  
    |  __ \  |  __ \  |  __ \
    | |__) | | |  | | | |__) |
    |  ___/  | |  | | |  ___/
    | |      | |__| | | |    
    |_|      |_____/  |_|    
                                                                    
  Version 0.0.1-SNAPSHOT
     
  Useful sbt tasks:
  a. pdp/run - Start backend server
  b. allCompile - Compile main and test sources
  c. prePush - Full pre-commit quality gate: compile, test, format, check dependencies
  d. fullCheck - Comprehensive project validation: prePush + dependency updates check
  ```

#### sbt-native-packager Packaging

- **[Repository](https://github.com/sbt/sbt-native-packager)**
- **What does it do?** Allows packaging the application into various formats for distribution:
  Docker images, deb/rpm packages for Linux, MSI for Windows, .exe, .app, and many others.
  This is critical for deploying the application in production.

## Step 3: Creating SBT Command Aliases

To avoid memorizing long sequences of commands, it's convenient to create short aliases for them.

1.  Create a file `aliases.sbt` in the project root (at the `build.sbt` level). SBT will automatically pick it up.
2.  Add the following aliases to the file:

    ```dotty
    // "allCompile" - Compile main and test code
    addCommandAlias("allCompile", "compile; Test / compile")
    // "fix" - Apply all fixes and formatting
    addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
    // "prePush" - Full check before committing code
    addCommandAlias(
      "prePush",
      "clean; allCompile; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
    )
    // "fullCheck" - Full check + look for library updates
    addCommandAlias("fullCheck", "prePush; dependencyUpdates")
    ```

**How to Use:**

- Before making a `git push`, run `sbt prePush`. This command:
    1. `clean` — cleans previous build artifacts.
    2. `allCompile` — compiles main and test code.
    3. `test` — runs all tests.
    4. `fix` — formats code and applies Scalafix fixes.
    5. `undeclaredCompileDependencies` and `unusedCompileDependencies` — checks declared dependencies for correctness.
- For a deep check, use `sbt fullCheck`, which additionally shows available library updates.

## Step 4: Managing Dependencies

To neatly manage libraries, especially in large projects, 
dependencies should be moved to a separate object.

1.  Create a file `project/Dependencies.scala`.
2.  Add code to it that declares dependencies in a structured way:

    ```dotty
    import sbt.*

    object Dependencies {
      // Scala version for the project
      val Scala = "3.9.0" // Specify the current version

      object cats {
        private val version       = "2.13.0"
        private val effectVersion = "3.7.1"
      
        val core         = "org.typelevel" %% "cats-core"          % version
        val effect       = "org.typelevel" %% "cats-effect"        % effectVersion
        val effectKernel = "org.typelevel" %% "cats-effect-kernel" % effectVersion
      }

      object weaver {
        private val version = "0.13.0"
      
        val cats       = "org.typelevel" %% "weaver-cats"       % version
        val scalacheck = "org.typelevel" %% "weaver-scalacheck" % version
      }

      // List of libraries for main code
      val pdpLibs: Seq[ModuleID] =
        Seq(
          cats.core,
          cats.effect,
          cats.effectKernel,
        )

      // List of libraries for tests (specified with `% Test` modifier)
      val testLibs: Seq[ModuleID] =
        Seq(
          weaver.cats
        ).map(_ % Test)
    }
    ```

3.  Now update the root `build.sbt` file to use these dependencies:

    ```dotty
    // Import dependencies
    import Dependencies.*
    
    lazy val pdp = (project in file("pdp"))
      // Plugins for generating front-end and docker image
      .enablePlugins(
        DockerPlugin,
        JavaAppPackaging
      )
      .settings(
        name := "pdp-backend",
        publish / skip := true,
        libraryDependencies ++= pdpLibs,
        libraryDependencies ++= testLibs
      )
      // Docker image settings
      .settings(
        Docker / packageName := "personal-development-plan",
        dockerExposedPorts ++= Seq(8080),
        dockerUpdateLatest := true
      )
      .settings(
        run / fork := true
      )
    
    lazy val root = (project in file("."))
      .aggregate(pdp)
      .settings(
        name := "pdp-root",
        publish / skip := true
      )
    ```

    In complex projects, a modular structure is recommended, where functionality is broken into separate pieces.
    For example, `pdp` is the module for personal plan development, and `root` is the root module.
    In this case, `root` is not an independent project, but simply brings all modules together.
    This allows easily building and testing individual parts of the project.

**Technology Stack Used (Typelevel Stack):**

- **[Cats Effect](https://typelevel.org/cats-effect/)** — a library for asynchronous 
  and parallel programming based on functional principles.
- **[Weaver Test](https://typelevel.org/weaver-test/overview/installation.html)** — a simple 
  and powerful testing framework, ideally suited for Cats Effect projects.

## Step 5: Configuring Compiler Options

Strict compiler settings help write higher quality code, 
turning potential runtime errors into compile-time errors.

Add the following block to your `build.sbt` (usually at the beginning of the file, as it applies to all subprojects: `ThisBuild`):

```dotty
ThisBuild / scalacOptions ++=
  Seq(
    // Warns about use of deprecated constructs
    "-deprecation",
    // Explains compilation errors in detail
    "-explain",
    // Turns unused imports into errors
    "-Wunused:imports",
    // Warns about places where the compiler made type assumptions
    "-unchecked",
    // Won't compile code with warnings.
    "-Werror",
    // Warns about use of experimental language features
    "-feature",
    // Warns if the significant result of an expression (not `Unit`) is ignored
    "-Wnonunit-statement"
  )
  
ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
```

## Step 6: Configuring the sbt-welcome Screen

Add the following configuration to your build.sbt:

```dotty
import sbtwelcome.*

logo :=
  raw"""
       |Welcome to
       |
       |  _____   _____    _____
       | |  __ \ |  __ \  |  __ \
       | | |__) || |  | | | |__) |
       | |  ___/ | |  | | |  ___/
       | | |     | |__| | | |
       | |_|     |_____/  |_|
       |
       |Version ${version.value}
   """.stripMargin

logoColor := scala.Console.MAGENTA

usefulTasks := Seq(
  UsefulTask("pdp/run", "Start backend server"),
  UsefulTask("allCompile", "Compile main and test sources"),
  UsefulTask(
    "prePush",
    "Full pre-commit quality gate: compile, test, format, check dependencies"
  ),
  UsefulTask(
    "fullCheck",
    "Comprehensive project validation: prePush + dependency updates check"
  )
)
```

How it works:

- `logo` — sets an ASCII art or text logo that will be displayed on SBT startup. 
- `logoColor` — sets the logo color in the console. Available colors: `scala.Console.RED`, `GREEN`, `YELLOW`, `BLUE`, `MAGENTA`, `CYAN`, `WHITE`, and others.
- `usefulTasks` — a list of commands that are displayed on the welcome screen with descriptions. 
  This helps new developers quickly learn what commands are available in the project.

## Step 7: Final Functionality Check

After configuring all tools and settings, make sure that 
the project compiles correctly and passes all checks.

### Preparing Project Structure

1. **Clean standard folders:** Delete the `src/main/scala` and `src/test/scala` folders
2. In the `pdp` module, create `src/main/scala` and `src/test/scala` folders
3. **Create a package:** In both folders, create the package `ru.scalabook.pdp`

### Running the Full Check

In the terminal, in the project root folder, run:

```dotty
sbt prePush
```

This command, which we defined earlier, will run a full check cycle:

1. **`clean`** — cleaning previous build artifacts
2. **`com`** — compiling main and test code
3. **`test`** — running all tests
4. **`fix`** — Scalafmt code formatting and applying Scalafix fixes
5. **`undeclaredCompileDependencies`** — checking for hidden dependencies
6. **`unusedCompileDependencies`** — finding unused dependencies 
    (for now, ignore the warning about unused libraries, as there is no code written yet)

> Warnings about unused libraries can be ignored for now, 
> because no code has been written yet, so there are no used libraries.

The project is fully ready for development.

## Conclusion

Great! A Scala 3 project has been successfully set up:

- **Automation:** Code formatting and refactoring happen on command
- **Code quality:** Strict compiler settings and linters help avoid errors
- **Structure:** Dependencies and settings are neatly organized
- **Functional stack:** Libraries like Cats Effect form the project's foundation

Now you can focus on writing code, and the tools will take care of its quality.

---

**Links:**

- [Source code repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
