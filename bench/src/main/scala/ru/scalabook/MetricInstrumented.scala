package ru.scalabook

import nl.grons.metrics4.scala.DefaultInstrumented
import nl.grons.metrics4.scala.Timer

trait MetricInstrumented extends DefaultInstrumented:
  protected val loading: Timer = metrics.timer("loading")

  def prettyPrint(period: Period): Unit =
    println(s"Число тестовых запусков = ${loading.count}")
    printInSec("Минимальное время", loading.min.toDouble, period)
    printInSec("Максимальное время", loading.max.toDouble, period)
    printInSec("Среднее время", loading.mean, period)
    printInSec("Стандартное отклонение", loading.stdDev, period)
    printInSec("75 персентиль", loading.snapshot.get75thPercentile, period)
    printInSec("95 персентиль", loading.snapshot.get95thPercentile, period)

  private def printInSec(label: String, nanos: Double, period: Period): Unit =
    period match
      case Period.NanoSecond =>
        println(f"$label = $nanos наносекунд")
      case Period.MicroSecond =>
        println(f"$label = ${nanos / 1000}%.3f микросекунд")
      case Period.MilliSecond =>
        println(f"$label = ${nanos / 1e6}%.3f миллисекунд")
      case Period.Second =>
        println(f"$label = ${nanos / 1e9}%.3f секунд")
end MetricInstrumented
