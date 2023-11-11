package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait GCDRingLaw extends CRingWithUnityLaw:
  def checkGCDRingLaw[A: GCDRing](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCRingWithUnityLaw(x, y, z) combine
      check(
        GCDRing[A]
          .times(GCDRing[A].gcd(x, y), GCDRing[A].lcm(x, y)) == GCDRing[A]
          .times(y, x),
        "gcd(a, b) * lcm(a, b) === a * b"
      ) combine
      check(
        GCDRing[A].gcd(GCDRing[A].gcd(x, y), z) == GCDRing[A]
          .gcd(x, GCDRing[A].gcd(y, z)),
        "gcd associativity"
      ) combine
      check(
        GCDRing[A].gcd(x, y) == GCDRing[A].gcd(y, x),
        "gcd commutative"
      ) combine
      check(
        GCDRing[A].lcm(GCDRing[A].lcm(x, y), z) == GCDRing[A]
          .lcm(x, GCDRing[A].lcm(y, z)),
        "lcm associativity"
      ) combine
      check(
        GCDRing[A].lcm(x, y) == GCDRing[A].lcm(y, x),
        "lcm commutative"
      )
