package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User
import models.daos.UserDAOSlickImpl

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(userDAO: UserDAOSlickImpl)(implicit ex: ExecutionContext)
extends IdentityService[User] {

  def retrieve(id: UUID) = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.save(user)

  def update(user: User) = userDAO.update(user)

}
