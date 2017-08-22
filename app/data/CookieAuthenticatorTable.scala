package data

import java.util.concurrent.TimeUnit

import com.github.tototoshi.slick.PostgresJodaSupport._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.FiniteDuration

final case class LiftedLoginInfo(
  providerID: Rep[String], providerKey: Rep[String]
)

final case class LiftedCookieAuthenticator(
  id: Rep[String],
  loginInfo: LiftedLoginInfo,
  lastUsedDateTime: Rep[DateTime],
  expirationDateTime: Rep[DateTime],
  idleTimeout: Rep[Option[FiniteDuration]],
  cookieMaxAge: Rep[Option[FiniteDuration]],
  fingerprint: Rep[Option[String]]
)

final class CookieAuthenticatorTable(tag: Tag)
extends Table[CookieAuthenticator](tag, "CookieAuthenticator") {

  implicit val finiteDurationColumnType = MappedColumnType.base[FiniteDuration, Long](
    duration => duration.toMillis,
    millis => FiniteDuration(millis, TimeUnit.MILLISECONDS)
  )

  implicit object LoginInfoShape extends CaseClassShape(
    LiftedLoginInfo.tupled, (LoginInfo.apply _).tupled
  )

  implicit object CookieAuthenticatorShape extends CaseClassShape(
    LiftedCookieAuthenticator.tupled, (CookieAuthenticator.apply _).tupled
  )

  def id = column[String]("id", O.PrimaryKey)
  def providerID = column[String]("providerID")
  def providerKey = column[String]("providerKey")
  def lastUsedDateTime = column[DateTime]("lastUsedDateTime")
  def expirationDateTime = column[DateTime]("expirationDateTime")
  def idleTimeout = column[Option[FiniteDuration]]("idleTimeout")
  def cookieMaxAge = column[Option[FiniteDuration]]("cookieMaxAge")
  def fingerprint = column[Option[String]]("fingerprint")

  override def * = LiftedCookieAuthenticator(
    id,
    LiftedLoginInfo(providerID, providerKey),
    lastUsedDateTime,
    expirationDateTime,
    idleTimeout,
    cookieMaxAge,
    fingerprint
  )

}
