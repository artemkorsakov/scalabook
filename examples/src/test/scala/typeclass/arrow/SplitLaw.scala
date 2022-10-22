package typeclass.arrow

import typeclass.common.Runner2

trait SplitLaw extends ComposeLaw:
  def checkSplitLaw[=>:[_, _]: Split: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A): Unit =
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(a)
