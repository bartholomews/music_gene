package controllers

import javax.inject.{Inject, Singleton}

import model.music.{JSONParser, MusicCollection}
import model.genetic.{GA, Playlist}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}

/**
  *
  */
@Singleton
class PlaylistController @Inject() extends Controller {

  // @see https://www.playframework.com/documentation/2.0/ScalaJsonRequests
  /*
  The reason why it’s not an Option is because the json body parser
  will validate that the request has a Content-Type of application/json,
  and send back a 415 Unsupported Media Type response if the request
  doesn't meet that expectation. Hence we don’t need to check again
  in our action code.
   */
  def generatePlaylist = Action(parse.json) { implicit request =>
    println(request.body.toString())
    JSONParser.parseRequest(request.body) match {
      case None => BadRequest("Json Request failed")
      case Some(p) =>
        println("READY TO ACCESS MONGO")
        val db = new MusicCollection(
          p.ids.flatMap(id => MongoController.readByID(id))
        )
        println("IDS RETRIEVED FROM MONGO")
        val (playlist, _) = GA.generatePlaylist(db, p.constraints, p.length)
        val js = createJsonResponse(p.name, playlist)
        Ok(js)
    }
  }

  def createJsonResponse(name: String, playlist: Playlist): JsValue = {
    val tracksID = Json.toJson(playlist.songs.map(s => s.id))
    val js: JsValue = Json.obj(
      "name" -> name,
      "ids" -> tracksID
      //  "response" -> statistics.get.distance // TODO
    )
    println("AJAX RESPONSE => " + js.toString())
    js
  }

  def test = Action { Ok(views.html.test()) }

}