package infrastructure

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import com.mohiva.play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.InMemoryAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.softwaremill.macwire._
import models.daos._
import models.services.{AuthTokenServiceImpl, UserServiceImpl}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.BuiltInComponents
import play.api.cache.ehcache.EhCacheComponents
import play.api.mvc.{BodyParsers, DefaultCookieHeaderEncoding}
import utils.auth.{CustomUnsecuredErrorHandler, DefaultEnv}

trait SilhouetteComponents
extends BuiltInComponents
with EhCacheComponents {

  lazy val defaultCookieHeaderEncoding = new DefaultCookieHeaderEncoding()

  lazy val securedErrorHandler: SecuredErrorHandler = wire[DefaultSecuredErrorHandler]

  lazy val securedRequestHandler : SecuredRequestHandler = wire[DefaultSecuredRequestHandler]
  lazy val unsecuredRequestHandler : UnsecuredRequestHandler = wire[DefaultUnsecuredRequestHandler]
  lazy val userAwareRequestHandler : UserAwareRequestHandler = wire[DefaultUserAwareRequestHandler]

  lazy val bpd: BodyParsers.Default = new BodyParsers.Default(playBodyParsers)

  lazy val securedAction: SecuredAction = wire[DefaultSecuredAction]
  lazy val unsecuredAction: UnsecuredAction = wire[DefaultUnsecuredAction]
  lazy val userAwareAction: UserAwareAction = wire[DefaultUserAwareAction]

  lazy val silhouetteDefaultEnv = wire[SilhouetteProvider[DefaultEnv]]
  lazy val unsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]

  lazy val userService = wire[UserServiceImpl]
  lazy val userDAO = wire[UserDAOImpl]
//  lazy val cacheLayer = wire[PlayCacheLayer]
  lazy val iDGenerator = new SecureRandomIDGenerator()
  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val eventBus = EventBus()
  lazy val clock = Clock()

  lazy val authTokenDAO = wire[AuthTokenDAOImpl]
  lazy val authTokenService = wire[AuthTokenServiceImpl]

    // Replace this with the bindings to your concrete DAOs
  lazy val delegableAuthInfoDAOPasswordInfo = new InMemoryAuthInfoDAO[PasswordInfo]
  lazy val delegableAuthInfoDAOOAuth1Info = new InMemoryAuthInfoDAO[OAuth1Info]
  lazy val delegableAuthInfoDAOOAuth2Info = new InMemoryAuthInfoDAO[OAuth2Info]
  lazy val delegableAuthInfoDAOOpenIDInfo = new InMemoryAuthInfoDAO[OpenIDInfo]

  /**
   * Provides the Silhouette environment.
   *
   * @return The Silhouette environment.
   */
  lazy val silhouetteEnvironment: Environment[DefaultEnv] = {
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the social provider registry.
   *
   * @return The Silhouette environment.
   */
  lazy val socialProviderRegistry: SocialProviderRegistry = {
    SocialProviderRegistry(Seq())
  }

  /**
   * Provides the signer for the OAuth1 token secret provider.
   *
   * @return The signer for the OAuth1 token secret provider.
   */
  lazy val oAuth1TokenSecretSigner: Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the OAuth1 token secret provider.
   *
   * @return The crypter for the OAuth1 token secret provider.
   */
  lazy val oAuth1TokenSecretCrypter: Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the signer for the CSRF state item handler.
   *
   * @return The signer for the CSRF state item handler.
   */
  lazy val cSRFStateItemSigner: Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the social state handler.
   *
   * @return The signer for the social state handler.
   */
  lazy val socialStateSigner: Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @return The signer for the authenticator.
   */
  lazy val authenticatorSigner: Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @return The crypter for the authenticator.
   */
  lazy val authenticatorCrypter: Crypter = {
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
      delegableAuthInfoDAOPasswordInfo,
      delegableAuthInfoDAOOAuth1Info,
      delegableAuthInfoDAOOAuth2Info,
      delegableAuthInfoDAOOpenIDInfo
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @return The authenticator service.
   */
  lazy val authenticatorService: AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(authenticatorCrypter)

    new CookieAuthenticatorService(config, None, authenticatorSigner, defaultCookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, iDGenerator, clock)
  }

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @return The OAuth1 token secret provider implementation.
   */
  lazy val oAuth1TokenSecretProvider: OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, oAuth1TokenSecretSigner, oAuth1TokenSecretCrypter, clock)
  }

  /**
   * Provides the CSRF state item handler.
   *
   * @return The CSRF state item implementation.
   */
  lazy val csrfStateItemHandler: CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, iDGenerator, cSRFStateItemSigner)
  }

  /**
   * Provides the social state handler.
   *
   * @return The social state handler implementation.
   */
  lazy val socialStateHandler: SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), socialStateSigner)
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

}
