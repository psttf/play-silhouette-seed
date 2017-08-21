package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import data.UserDBIO
import models.User
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class UserDAOSlickImpl(dbConfig: DatabaseConfig[JdbcProfile]) {

  def find(loginInfo: LoginInfo) =
    dbConfig.db.run(UserDBIO.findOne(loginInfo))

  def find(userID: UUID) =
    dbConfig.db.run(UserDBIO.lookup(userID))

  def save(user: User) =
    dbConfig.db.run(UserDBIO.save(user))

  def update(user: User) =
    dbConfig.db.run(UserDBIO.update(user))

}
