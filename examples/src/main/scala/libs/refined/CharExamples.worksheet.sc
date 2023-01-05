import eu.timepit.refined.refineV
import eu.timepit.refined.char.*

refineV[Digit]('0')
refineV[Digit]('a')
refineV[Digit]('Z')
refineV[Digit](' ')
refineV[Digit]('.')

refineV[Letter]('0')
refineV[Letter]('a')
refineV[Letter]('Z')
refineV[Letter](' ')
refineV[Letter]('.')

refineV[LetterOrDigit]('0')
refineV[LetterOrDigit]('a')
refineV[LetterOrDigit]('Z')
refineV[LetterOrDigit](' ')
refineV[LetterOrDigit]('.')

refineV[LowerCase]('0')
refineV[LowerCase]('a')
refineV[LowerCase]('Z')
refineV[LowerCase](' ')
refineV[LowerCase]('.')

refineV[UpperCase]('0')
refineV[UpperCase]('a')
refineV[UpperCase]('Z')
refineV[UpperCase](' ')
refineV[UpperCase]('.')

refineV[Whitespace]('0')
refineV[Whitespace]('a')
refineV[Whitespace]('Z')
refineV[Whitespace](' ')
refineV[Whitespace]('.')
