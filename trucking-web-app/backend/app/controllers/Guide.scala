package controllers

import javax.inject._

import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Guide @Inject() extends Controller {

  /**
    * Create an Action to render an HTML page with the main guiding message.
    */
  def index = Action {
    //Ok(views.html.guide.index("sdf"))
    Ok("")
  }

  def storm = Action {
    //Ok(views.html.guide.storm())
    Ok("")
  }

  def monitor = Action {
    //Ok(views.html.guide.monitor())
    Ok("")
  }

}
