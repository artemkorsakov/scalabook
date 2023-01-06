import libs.refined.Name
import libs.refined.Name.{ *, given }

"€‡™µ".toName
"12345".toName
"Alyona".toName
"Алёна18".toName
"алёна".toName
"Алёна".toName

val name0: Name = "€‡™µ".toName
val name1: Name = "12345".toName
val name2: Name = "Alyona".toName
val name3: Name = "Алёна18".toName
val name4: Name = "алёна".toName
val name5: Name = "Алёна".toName
