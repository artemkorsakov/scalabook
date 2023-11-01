package ru.scalabook.typeclass.monoid

trait GCDRingLaw extends CRingWithUnityLaw:
  def checkGCDRingLaw[A: GCDRing](x: A, y: A, z: A): Unit =
    checkCRingWithUnityLaw(x, y, z)
    assertEquals(
      GCDRing[A].times(GCDRing[A].gcd(x, y), GCDRing[A].lcm(x, y)),
      GCDRing[A].times(y, x),
      "gcd(a, b) * lcm(a, b) === a * b"
    )
    assertEquals(
      GCDRing[A].gcd(GCDRing[A].gcd(x, y), z),
      GCDRing[A].gcd(x, GCDRing[A].gcd(y, z)),
      "gcd associativity"
    )
    assertEquals(
      GCDRing[A].gcd(x, y),
      GCDRing[A].gcd(y, x),
      "gcd commutative"
    )
    assertEquals(
      GCDRing[A].lcm(GCDRing[A].lcm(x, y), z),
      GCDRing[A].lcm(x, GCDRing[A].lcm(y, z)),
      "lcm associativity"
    )
    assertEquals(
      GCDRing[A].lcm(x, y),
      GCDRing[A].lcm(y, x),
      "lcm commutative"
    )
