package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAO

import scala.concurrent.{ ExecutionContext, Future }

class UserServiceImpl (userDAO: UserDAO)(implicit ex: ExecutionContext) extends UserService {

  def retrieve(id: UUID) = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.save(user)

  def update(user: User) = userDAO.update(user)

}
