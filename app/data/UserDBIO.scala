package data

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.User
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object UserDBIO {

  def lookup(key: UUID): DBIO[Option[User]] =
    for {
      queried <- UserQueries.byUserID(key).result
      result <-
        if (queried.size > 1)
          DBIO failed new Exception(s"Many users with id $key")
        else
          DBIO successful queried.headOption
    } yield result

  def byEmail(email: String) = UserQueries.byEmail(email).result

  def findOne(loginInfo: LoginInfo): DBIO[Option[User]] =
    for {
      queried <-
        UserQueries.findOne((loginInfo.providerID, loginInfo.providerKey)).result
      result <-
        if (queried.size > 1)
          DBIO failed new Exception(s"Many users with id $loginInfo")
        else
          DBIO successful queried.headOption
    } yield result

  def save(entity: User): DBIO[User] = for {
    _ <- UserQueries.table += entity
    maybeSaved  <- findOne(entity.loginInfo)
    saved <- maybeSaved map DBIO.successful getOrElse DBIO.failed(
      new Exception(s"Cannot obtain entity after save: ${entity.loginInfo}")
    )
  } yield saved

  def update(entity: User): DBIO[User] = for {
    count <- UserQueries.byUserID(entity.userID).update(entity)
    result <-
      if (count < 1) DBIO failed new Exception("None updated")
      else DBIO successful entity
  } yield result

  def update(
    loginInfo: LoginInfo, passwordInfo: PasswordInfo
  ): DBIO[Int] =
    UserQueries
      .passwordInfoByLoginInfo((loginInfo.providerID, loginInfo.providerKey))
      .update((
        passwordInfo.hasher,
        passwordInfo.password,
        passwordInfo.salt
      ))

}
