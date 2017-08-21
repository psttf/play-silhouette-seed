package infrastructure

import akka.actor.Props
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.softwaremill.macwire._
import jobs.{AuthTokenCleaner, AuthTokenCleanerWrapper, Scheduler}
import models.daos._
import models.services.{AuthTokenServiceImpl, UserServiceImpl}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.BuiltInComponents
import play.api.cache.ehcache.EhCacheComponents
import play.api.db.slick.{DbName, SlickComponents}
import play.api.mvc.{BodyParsers, DefaultCookieHeaderEncoding}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv}

//noinspection ScalaUnusedSymbol
trait SilhouetteComponents
extends BuiltInComponents
with EhCacheComponents
with SlickComponents {

  private lazy val dbConfig: DatabaseConfig[JdbcProfile] =
    slickApi.dbConfig(DbName("default"))

  private[this] lazy val defaultCookieHeaderEncoding = new DefaultCookieHeaderEncoding()

  private[this] lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]

  private[this] lazy val securedRequestHandler : SecuredRequestHandler = wire[DefaultSecuredRequestHandler]
  private[this] lazy val unsecuredRequestHandler : UnsecuredRequestHandler = wire[DefaultUnsecuredRequestHandler]
  private[this] lazy val userAwareRequestHandler : UserAwareRequestHandler = wire[DefaultUserAwareRequestHandler]

  private[this] lazy val bpd: BodyParsers.Default = new BodyParsers.Default(playBodyParsers)

  private[this] lazy val securedAction: SecuredAction = wire[DefaultSecuredAction]
  private[this] lazy val unsecuredAction: UnsecuredAction = wire[DefaultUnsecuredAction]
  private[this] lazy val userAwareAction: UserAwareAction = wire[DefaultUserAwareAction]

  lazy val silhouetteDefaultEnv = wire[SilhouetteProvider[DefaultEnv]]
  private[this] lazy val unsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]

  lazy val userService = wire[UserServiceImpl]
  private[this] lazy val userDAO = wire[UserDAOSlickImpl]
//  private[this] lazy val cacheLayer = wire[PlayCacheLayer]
  private[this] lazy val iDGenerator = new SecureRandomIDGenerator()
  private[this] lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  private[this] lazy val eventBus = EventBus()
  lazy val clock = Clock()

  private[this] lazy val authTokenDAO = wire[AuthTokenDAOSlickImpl]
  lazy val authTokenService: AuthTokenServiceImpl = wire[AuthTokenServiceImpl]

  // Replace this with the bindings to your concrete DAOs
  private[this] lazy val delegableAuthInfoDAOPasswordInfo = wire[PasswordInfoDAOSlickImpl]

  /**
   * Provides the Silhouette environment.
   *
   * @return The Silhouette environment.
   */
  private[this] lazy val silhouetteEnvironment: Environment[DefaultEnv] = {
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @return The signer for the authenticator.
   */
  private[this] lazy val authenticatorSigner: Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @return The crypter for the authenticator.
   */
  private[this] lazy val authenticatorCrypter: Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the auth info repository.
   *
   * @return The auth info repository instance.
   */
  lazy val authInfoRepository: AuthInfoRepository = {
    new DelegableAuthInfoRepository(
      delegableAuthInfoDAOPasswordInfo
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @return The authenticator service.
   */
  private[this] lazy val authenticatorService: AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(authenticatorCrypter)

    new CookieAuthenticatorService(
      config,
      Some(wire[CookieAuthenticatorRepository]),
      authenticatorSigner, defaultCookieHeaderEncoding,
      authenticatorEncoder, fingerprintGenerator, iDGenerator, clock
    )
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  lazy val passwordHasherRegistry: PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
   * Provides the credentials provider.
   *
   * @return The credentials provider.
   */
  lazy val credentialsProvider: CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  private[this] lazy val authTokenCleaner =
    AuthTokenCleanerWrapper(actorSystem.actorOf(Props(wire[AuthTokenCleaner])))

  private[this] lazy val scheduler = wire[Scheduler]

  scheduler

}
