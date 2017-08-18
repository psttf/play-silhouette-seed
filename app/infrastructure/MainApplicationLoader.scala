package infrastructure

import com.softwaremill.macwire._

import _root_.controllers._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasher, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.services.AuthTokenServiceImpl
import org.webjars.play.{RequireJS, WebJarComponents}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n.I18nComponents
import play.api.libs.mailer.MailerComponents
import play.api.mvc._
import play.filters.HttpFiltersComponents

import scala.concurrent.duration.{Duration, FiniteDuration}

import _root_.controllers.ApplicationController
import _root_.controllers.ApplicationController
import _root_.controllers.SocialAuthController
import _root_.controllers.SignUpController
import _root_.controllers.SignUpController
import _root_.controllers.SignInController
import _root_.controllers.SignInController
import _root_.controllers.ForgotPasswordController
import _root_.controllers.ForgotPasswordController
import _root_.controllers.ResetPasswordController
import _root_.controllers.ResetPasswordController
import _root_.controllers.ChangePasswordController
import _root_.controllers.ChangePasswordController
import _root_.controllers.ActivateAccountController
import _root_.controllers.ActivateAccountController
import _root_.controllers.Assets

final class MainApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new MainComponents(context).application
  }
}

//noinspection ScalaUnusedSymbol
final class MainComponents(context: Context)
extends BuiltInComponentsFromContext(context)
with HttpFiltersComponents
with MailerComponents
with SilhouetteComponents
with WebJarComponents
with AssetsComponents {

  implicit val implicitWebJarsUtil = webJarsUtil
  implicit val implicitAssetsFinder = assetsFinder

  lazy val config = configuration.underlying

  lazy val applicationController = wire[ApplicationController]
  lazy val socialAuthController = wire[SocialAuthController]
  lazy val signUpController = wire[SignUpController]
  lazy val signInController = wire[SignInController]
  lazy val forgotPasswordController = wire[ForgotPasswordController]
  lazy val resetPasswordController = wire[ResetPasswordController]
  lazy val changePasswordController = wire[ChangePasswordController]
  lazy val activateAccountController = wire[ActivateAccountController]

  private lazy val requireJS = wire[RequireJS]
  private lazy val webjarsRouter = wire[webjars.Routes]

  private lazy val routePrefix = "/"
  lazy val router = wire[_root_.router.Routes]

}
