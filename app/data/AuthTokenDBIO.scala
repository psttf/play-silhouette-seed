package data

import java.util.UUID

import models.AuthToken
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object AuthTokenDBIO {

  def lookup(key: UUID): DBIO[Option[AuthToken]] =
    for {
      queried <- AuthTokenQueries.byId(key).result
      result <-
        if (queried.size > 1)
          DBIO failed new Exception(s"Many tokens with id $key")
        else
          DBIO successful queried.headOption
    } yield result


  def findExpired(dateTime: DateTime): DBIO[Seq[AuthToken]] =
    AuthTokenQueries.expired(dateTime).result

  def save(entity: AuthToken): DBIO[AuthToken] =
    for { _ <- AuthTokenQueries.table += entity } yield entity

  def remove(id: UUID): DBIO[Unit] =
    AuthTokenQueries.byId(id).delete map ( _ => () )

}
