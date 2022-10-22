package typeclass.arrow

trait SplitLaw extends ComposeLaw:
  def checkSplitLaw[=>:[_, _]: Split, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(runAA: A =>: A => A, runAD: A =>: D => D): Unit =
    val ins = summon[Split[=>:]]
    import ins.{compose, split}
    //checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(runAA, runAD)
