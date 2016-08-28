package model.music

/**
  *
  */
trait Attribute {
  def value: Any
}

trait TextAttribute extends Attribute {
  override def value: String
}

trait TimeAttribute extends Attribute {
  override def value: Int
}
trait AudioAttribute extends Attribute {
  val min: Double = 0.0
  val max: Double = 1.0
  override def value: Double
}

case class Title(override val value: String) extends TextAttribute
case class Album(override val value: String) extends TextAttribute
case class Artist(override val value: String) extends TextAttribute
case class SongType(value: String) extends TextAttribute
case class Preview_URL(value: String) extends TextAttribute

case class Year(value: Int) extends Attribute

case class Genre(value: GenreType) extends Attribute
case class Language(value: LanguageType) extends Attribute
case class Mood(value: MoodType) extends Attribute

case class Duration(value: Double) extends AudioAttribute {
  override val max: Double = 1800000
}
case class Acousticness(value: Double) extends AudioAttribute
case class Danceability(value: Double) extends AudioAttribute
case class Energy(value: Double) extends AudioAttribute
case class Instrumentalness(value: Double) extends AudioAttribute
case class Liveness(value: Double) extends AudioAttribute
case class Loudness(value: Double) extends AudioAttribute {
  override val min: Double = -60
  override val max: Double = 0
}
case class Speechiness(value: Double) extends AudioAttribute
case class Tempo(value: Double) extends AudioAttribute {
  override val max: Double = 240
}
case class Valence(value: Double) extends AudioAttribute

case class Time_Signature(value: Int) extends TimeAttribute
case class Mode(value: Int) extends TimeAttribute
case class Key(value: Int) extends TimeAttribute

trait GenreType
case object Popular extends GenreType
case object Rock extends GenreType
case object HipHop extends GenreType

trait LanguageType
case object Mandarin extends LanguageType
case object English extends LanguageType
case object Italian extends LanguageType

trait MoodType
case object Happy extends MoodType
case object Sad extends MoodType
