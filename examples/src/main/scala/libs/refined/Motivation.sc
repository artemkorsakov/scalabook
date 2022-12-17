import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

type Name = String Refined MatchesRegex["[А-ЯЁ]{1}[а-яё]+"]
object Name extends RefinedTypeOps[Name, String]

Name.from("€‡™µ")
Name.from("12345")
Name.from("Alyona")
Name.from("Алёна18")
Name.from("алёна")
Name.from("Алёна")

Name.unapply("€‡™µ")
Name.unapply("12345")
Name.unapply("Alyona")
Name.unapply("Алёна18")
Name.unapply("алёна")
Name.unapply("Алёна")
