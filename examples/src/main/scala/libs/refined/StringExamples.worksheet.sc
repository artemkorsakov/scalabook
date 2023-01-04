import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

import scala.util.Random

refineV[EndsWith["ing"]]("String")
refineV[EndsWith["in"]]("String")

refineV[StartsWith["Str"]]("String")
refineV[StartsWith["tr"]]("String")

refineV[MatchesRegex["[0-9]+"]]("123.")
refineV[MatchesRegex["[0-9]+"]]("123")

refineV[Regex]("[0-9]+")
refineV[Regex]("[0--9]+")

refineV[IPv4]("192.0.2.235")
refineV[IPv4]("2001:0db8:11a3:09d7:1f34:8a2e:07a0:765d")

refineV[IPv6]("192.0.2.235")
refineV[IPv6]("2001:0db8:11a3:09d7:1f34:8a2e:07a0:765d")

refineV[Uri]("http://www.ics.uci.edu/pub/ietf/uri/#Related")
refineV[Uri]("http   www.ics.uci.edu/pub/ietf/uri/#Related")

refineV[Url]("http://www.ics.uci.edu")
refineV[Url]("htp://www.ics.uci.edu")

refineV[Uuid](java.util.UUID.randomUUID().toString)
refineV[Uuid]("htp://www.ics.uci.edu")

refineV[XPath]("//a[class='active']")
refineV[XPath]("//a class='active']")

refineV[Trimmed](" string ")
refineV[Trimmed](" string")
refineV[Trimmed]("string ")
refineV[Trimmed]("string")

refineV[HexStringSpec](Random.nextInt().toHexString)
refineV[HexStringSpec]("0123456789abcdef")
refineV[HexStringSpec]("0123456789abcdefg")

refineV[ValidByte]("-129")
refineV[ValidByte]("-128")
refineV[ValidByte]("0")
refineV[ValidByte]("127")
refineV[ValidByte]("128")

refineV[ValidShort]("-32769")
refineV[ValidShort]("-32768")
refineV[ValidShort]("0")
refineV[ValidShort]("32767")
refineV[ValidShort]("32768")

refineV[ValidInt]("-2147483649")
refineV[ValidInt]("-2147483648")
refineV[ValidInt]("0")
refineV[ValidInt]("2147483647")
refineV[ValidInt]("2147483648")

refineV[ValidLong]("-9223372036854775809")
refineV[ValidLong]("-9223372036854775808")
refineV[ValidLong]("0")
refineV[ValidLong]("9223372036854775807")
refineV[ValidLong]("9223372036854775808")

refineV[ValidFloat]("0.0ff")
refineV[ValidFloat]("0.0f")
refineV[ValidFloat]("0.0")

refineV[ValidDouble]("0.0dd")
refineV[ValidDouble]("0.0d")
refineV[ValidDouble]("0.0")

refineV[ValidBigInt]("9223372036854775808")
refineV[ValidBigInt]("0.0")
refineV[ValidBigInt]("string")

refineV[ValidBigDecimal]("9223372036854775808")
refineV[ValidBigDecimal]("0.0")
refineV[ValidBigDecimal]("string")
