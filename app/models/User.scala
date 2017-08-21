package models

import java.util.UUID

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import org.joda.time.{DateTime, DateTimeZone}

case class User(
  userID: UUID,
  providerID: String,
  providerKey: String,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  activated: Boolean,
  hasher: String,
  password: String,
  salt: Option[String] = None
) extends Identity {

  lazy val loginInfo = LoginInfo(providerID, providerKey)

  lazy val passwordInfo = PasswordInfo(hasher, password, salt)

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name = fullName.orElse {
    firstName -> lastName match {
      case (Some(f), Some(l)) => Some(f + " " + l)
      case (Some(f), None) => Some(f)
      case (None, Some(l)) => Some(l)
      case _ => None
    }
  }

  def freshToken =
    AuthToken(
      UUID.randomUUID(),
      userID,
      DateTime.now.withZone(DateTimeZone.UTC)
        .plusSeconds(AuthToken.defaultExpiry.toSeconds.toInt)
    )

}

object User {
  def withLoginInfo(
    userID: UUID,
    loginInfo: LoginInfo,
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    activated: Boolean,
    hasher: String,
    password: String,
    salt: Option[String] = None
  ) = apply(
    userID,
    loginInfo.providerID,
    loginInfo.providerKey,
    firstName,
    lastName,
    fullName,
    email,
    activated,
    hasher,
    password,
    salt
  )
}
