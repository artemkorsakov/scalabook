class BitMap
class InkJet

class Printer:
  type PrinterType
  def print(bits: BitMap): Unit = println("Printer.print()")
  def status: List[String] = ???

class Scanner:
  def scan(): BitMap =
    println("Scanner.scan()")
    BitMap()
  def status: List[String] = ???

class Copier:
  private val printUnit = new Printer { type PrinterType = InkJet }
  private val scanUnit = new Scanner

  export scanUnit.scan
  export printUnit.{status as _, *}

  def status: List[String] = printUnit.status ++ scanUnit.status

val copier = new Copier
copier.print(copier.scan())
