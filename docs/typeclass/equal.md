# Группа Equal

Данная группа используется для описания типов, экземпляры которых можно сравнивать между собой.

## Схема

```mermaid
classDiagram
    class Equal~A~{
        +equal(x: A, y: A) Boolean
    }
    class Ordering{ 
        <<enumeration>> 
        LT 
        EQ 
		GT
    }   
    class Order~A~{
        +order(x: A, y: A) Ordering
    }
    Ordering .. Order 
    Equal <|-- Order
    class Bounded~A~{
        +min: A
        +max: A
    }
    Order <|-- Bounded
    class Sign{ 
        <<enumeration>> 
        Positive 
        Zero 
		Negative
    }   
    class Signed~A~{
        +sign(x: A) Sign
    }
    Sign .. Signed
    Order <|-- Signed 
    class Predecessible~A~{
        +pred(x: A) A
    }
    class Successible~A~{
        +succ(x: A) A
    }
    class Bothable~A~
    Predecessible <|-- Bothable 
    Successible <|-- Bothable     
    class Enum~A~{
        +min: Option[A]
        +max: Option[A]
    }
    Order <|-- Enum 
    Bothable <|-- Enum  
    Bounded .. Enum    
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Equal.html)
- [Scalaz](https://scalaz.github.io/7/typeclass/Equal.html)
