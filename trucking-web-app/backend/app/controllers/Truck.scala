package controllers

import javax.inject._

import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Truck @Inject() extends Controller {

  /**
    * Create an Action to render an HTML page with the main guiding message.
    */
  def monitor = Action {
    //Ok(views.html.guide.index("Monitor"))
    Ok("")
  }

}
