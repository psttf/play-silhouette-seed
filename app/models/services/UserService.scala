package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  def retrieve(id: UUID): Future[Option[User]]
  def save(user: User): Future[User]
  def update(user: User): Future[User]

}
