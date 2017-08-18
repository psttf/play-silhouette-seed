package data

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import slick.dbio.DBIO

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object CookieAuthenticatorDBIO {

  def find(
    id: String
  ): DBIO[Option[CookieAuthenticator]] =
    for {
      queried <- CookieAuthenticatorQueries.byId(id).result
      result <-
        if (queried.size > 1)
          DBIO failed new Exception(s"Many CookieAuthenticators with id $id")
        else
          DBIO successful queried.headOption
    } yield result


  def add(
    entity: CookieAuthenticator
  ): DBIO[CookieAuthenticator] =
    for { _ <- CookieAuthenticatorQueries.table += entity } yield entity

  def update(
    authenticator: CookieAuthenticator
  ): DBIO[CookieAuthenticator] =
    CookieAuthenticatorQueries.byId(authenticator.id) update authenticator map
      ( _ => authenticator )

  def remove(
    id: String
  ): DBIO[Unit] =
    CookieAuthenticatorQueries.byId(id).delete map ( _ => () )

}
