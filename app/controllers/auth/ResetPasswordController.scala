package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.AssetsFinder
import data.{AuthTokenDBIO, UserDBIO}
import forms.ResetPasswordForm
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import silhouetteIntegration.{DefaultEnv, UserIdentityService}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ResetPasswordController (
  components: ControllerComponents,
  dbConfig: DatabaseConfig[JdbcProfile],
  silhouette: Silhouette[DefaultEnv],
  userService: UserIdentityService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Reset Password` page.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def view(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    dbConfig.db.run(AuthTokenDBIO.lookup(token)).map {
      case Some(_) => Ok(views.html.auth.resetPassword(ResetPasswordForm.form, token))
      case None => Redirect(routes.SignInController.view()).flashing("danger" -> Messages("invalid.reset.link"))
    }
  }

  /**
   * Resets the password.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def submit(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    dbConfig.db.run(AuthTokenDBIO.lookup(token)).flatMap {
      case Some(authToken) =>
        ResetPasswordForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.auth.resetPassword(form, token))),
          password =>
            dbConfig.db.run(UserDBIO.lookup(authToken.userID)) flatMap {
              case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
                val passwordInfo = passwordHasherRegistry.current.hash(password)
                authInfoRepository.update[PasswordInfo](user.loginInfo, passwordInfo).map { _ =>
                  Redirect(routes.SignInController.view()).flashing("success" -> Messages("password.reset"))
                }
              case _ => Future.successful(Redirect(routes.SignInController.view()).flashing("danger" -> Messages("invalid.reset.link")))
            }
        )
      case None => Future.successful(Redirect(routes.SignInController.view()).flashing("danger" -> Messages("invalid.reset.link")))
    }
  }
}
