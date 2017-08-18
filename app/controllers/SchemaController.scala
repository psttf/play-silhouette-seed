package controllers

import data.{AuthTokenQueries, CookieAuthenticatorQueries, UserDBIO, UserQueries}
import play.api.mvc._

final class SchemaController(cc: ControllerComponents)
extends AbstractController(cc) {

  def show = Action { implicit request =>
    Ok(
      s"""${UserQueries.schema.createStatements.mkString("\n")};
         |
         |${AuthTokenQueries.schema.createStatements.mkString("\n")};
         |
         |${CookieAuthenticatorQueries.schema.createStatements.mkString("\n")};
         |""".stripMargin
    )
  }

}
