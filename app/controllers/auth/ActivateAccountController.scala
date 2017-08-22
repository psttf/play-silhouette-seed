package controllers.auth

import java.net.URLDecoder
import java.util.UUID

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import data.{AuthTokenDBIO, UserDBIO}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import silhouetteIntegration.{DefaultEnv, UserIdentityService}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ActivateAccountController (
  components: ControllerComponents,
  dbConfig: DatabaseConfig[JdbcProfile],
  silhouette: Silhouette[DefaultEnv],
  userService: UserIdentityService,
  mailerClient: MailerClient
)(
  implicit
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  def send(email: String) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    val decodedEmail = URLDecoder.decode(email, "UTF-8")
    val loginInfo = LoginInfo(CredentialsProvider.ID, decodedEmail)
    val result = Redirect(routes.SignInController.view()).flashing("info" -> Messages("activation.email.sent", decodedEmail))

    dbConfig.db.run(UserDBIO.findOne(loginInfo)).flatMap {
      case Some(user) if !user.activated =>
        dbConfig.db.run(AuthTokenDBIO save user.freshToken).map { authToken =>
          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()

          mailerClient.send(Email(
            subject = Messages("email.activate.account.subject"),
            from = Messages("email.from"),
            to = Seq(decodedEmail),
            bodyText = Some(views.txt.auth.emails.activateAccount(user, url).body),
            bodyHtml = Some(views.html.auth.emails.activateAccount(user, url).body)
          ))
          result
        }
      case None => Future.successful(result)
    }
  }

  def activate(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    dbConfig.db.run(AuthTokenDBIO.lookup(token)).flatMap {
      case Some(authToken) =>
        dbConfig.db.run(UserDBIO.lookup(authToken.userID)) flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            dbConfig.db.run(UserDBIO.update(user.copy(activated = true))) map {
              _ =>
                Redirect(routes.SignInController.view()).flashing("success" -> Messages("account.activated"))
            }
          case _ => Future.successful(Redirect(routes.SignInController.view()).flashing("danger" -> Messages("invalid.activation.link")))
        }
      case None => Future.successful(Redirect(routes.SignInController.view()).flashing("danger" -> Messages("invalid.activation.link")))
    }
  }
}
