package controllers

import javax.inject._

import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Go @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def ambari = Action {
    Redirect("sandbox.hortonworks.com:8080") //TODO: extract both host and port
  }

  def nifi = Action {
    Redirect("sandbox.hortonworks.com:8080") //TODO: extract both host and port
  }

}
