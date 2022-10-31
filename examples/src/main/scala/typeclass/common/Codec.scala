package typeclass.common

trait Codec[A]:
  def encode(value: A): String
  def decode(value: String): A
