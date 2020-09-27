package views.spotify.requests

import io.bartholomews.musicgene.controllers.http.codecs.withDiscriminator
import play.api.libs.json.{Json, Reads}

sealed trait SpotifyAttribute
object SpotifyAttribute {
  implicit val reads: Reads[SpotifyAttribute] = withDiscriminator.reads[SpotifyAttribute]
}

case class Loudness(value: Double, min: Option[Double], max: Option[Double]) extends SpotifyAttribute
object Loudness {
  implicit val reads: Reads[Loudness] = Json.reads[Loudness]
}

case class Tempo(value: Double, min: Option[Double], max: Option[Double]) extends SpotifyAttribute
object Tempo {
  implicit val reads: Reads[Tempo] = Json.reads[Tempo]
}

case class Acousticness(value: Double) extends SpotifyAttribute
object Acousticness {
  implicit val reads: Reads[Acousticness] = Json.reads[Acousticness]
}