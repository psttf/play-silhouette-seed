package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

trait UserDAO {

  def find(loginInfo: LoginInfo): Future[Option[User]]
  def find(userID: UUID): Future[Option[User]]
  def save(user: User): Future[User]
  def update(user: User): Future[User]

}
