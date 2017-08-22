package data

import java.util.UUID

import models.AuthToken
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import com.github.tototoshi.slick.PostgresJodaSupport._

final class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, "AuthToken") {

  def id = column[UUID]("id", O.PrimaryKey)
  def userID = column[UUID]("userID")
  def expiry = column[DateTime]("expiry")

  override def * : ProvenShape[AuthToken] =
    (
      id,
      userID,
      expiry
    ) <>
      ((AuthToken.apply _).tupled, AuthToken.unapply)

}

object AuthTokenQueries {

  val table = TableQuery[AuthTokenTable]

  val schema = table.schema

  val byId = Compiled( (id: Rep[UUID]) =>
    table.filter(_.id === id)
  )

  val expired = Compiled( (expiry: Rep[DateTime]) =>
    table.filter(_.expiry < expiry)
  )

}
