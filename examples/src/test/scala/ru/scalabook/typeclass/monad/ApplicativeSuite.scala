package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.Functions.given
import ru.scalabook.typeclass.common.*
import ru.scalabook.typeclass.common.Runner1.stateRunner
import ru.scalabook.typeclass.monad.Applicative.given

class ApplicativeSuite extends ScalaCheckSuite, ApplicativeLaw:
  property("idApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[Id, Int, String, Boolean](x)
    }
  }

  property("optionApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[Option, Int, String, Boolean](x)
    }
  }

  property("listApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[List, Int, String, Boolean](x)
    }
  }

  property("eitherApplicative[E] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> Either[String, X], Int, String, Boolean](x)
    }
  }

  property("writerApplicative[W] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> Writer[String, X], Int, String, Boolean](x)
    }
  }

  property("stateApplicative[S] должен удовлетворять законам Applicative") {
    forAll { (x: Int, s: String) =>
      given Runner1[[X] =>> State[String, X]] = stateRunner[String](s)
      checkApplicativeLawWithRunner[[X] =>> State[
        String,
        X
      ], Int, String, Boolean](x)
    }
  }

  property("compositeApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> Option[List[X]], Int, String, Boolean](x)
    }
  }

  property("tupleApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> (Option[X], List[X]), Int, String, Boolean](x)
    }
  }

  property("nestedApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> Nested[Id, Option, X], Int, String, Boolean](
        x
      )
    }
  }

  property("ioApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[IO, Int, String, Boolean](x)
    }
  }
