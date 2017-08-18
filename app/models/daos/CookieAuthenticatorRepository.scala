package models.daos

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import data.CookieAuthenticatorDBIO
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class CookieAuthenticatorRepository(dbConfig: DatabaseConfig[JdbcProfile])
extends AuthenticatorRepository[CookieAuthenticator] {

  override def find(
    id: String
  ): Future[Option[CookieAuthenticator]] =
    dbConfig.db.run(CookieAuthenticatorDBIO.find(id))

  override def add(
    authenticator: CookieAuthenticator
  ): Future[CookieAuthenticator] =
    dbConfig.db.run(CookieAuthenticatorDBIO.add(authenticator))

  override def update(
    authenticator: CookieAuthenticator
  ): Future[CookieAuthenticator] =
    dbConfig.db.run(CookieAuthenticatorDBIO.update(authenticator))

  override def remove(
    id: String
  ): Future[Unit] =
    dbConfig.db.run(CookieAuthenticatorDBIO.remove(id))


}
