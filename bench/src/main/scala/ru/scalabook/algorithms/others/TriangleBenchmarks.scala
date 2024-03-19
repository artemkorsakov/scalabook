package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.geometry.{PointOnTriangleType, Triangle}

@main def getPointOnTriangleTypeBench: PointOnTriangleType =
  Triangle((-340, 495), (-153, -910), (835, -947)).getPointOnTriangleType((
    0,
    0
  ))
