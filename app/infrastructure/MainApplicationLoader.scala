package infrastructure

import _root_.controllers.{ActivateAccountController, ApplicationController, ChangePasswordController, ForgotPasswordController, ResetPasswordController, SignInController, SignUpController, _}
import com.softwaremill.macwire._
import org.webjars.play.{RequireJS, WebJarComponents}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.libs.mailer.MailerComponents
import play.filters.HttpFiltersComponents

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
with AssetsComponents
with SlickEvolutionsComponents
with EvolutionsComponents
with DatabaseComponents {

  private implicit val implicitWebJarsUtil = webJarsUtil
  private implicit val implicitAssetsFinder = assetsFinder

  lazy val config = configuration.underlying

  private lazy val applicationController = wire[ApplicationController]
  private lazy val signUpController = wire[SignUpController]
  private lazy val signInController = wire[SignInController]
  private lazy val forgotPasswordController = wire[ForgotPasswordController]
  private lazy val resetPasswordController = wire[ResetPasswordController]
  private lazy val changePasswordController = wire[ChangePasswordController]
  private lazy val activateAccountController = wire[ActivateAccountController]
  private lazy val schemaController = wire[SchemaController]

  private lazy val requireJS = wire[RequireJS]
  private lazy val webjarsRouter = wire[webjars.Routes]

  private lazy val routePrefix = "/"
  lazy val router = wire[_root_.router.Routes]

  applicationEvolutions

}
