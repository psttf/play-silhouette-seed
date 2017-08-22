package silhouetteIntegration

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import data.UserDBIO
import models.User
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserIdentityService(
  dbConfig: DatabaseConfig[JdbcProfile],
)(implicit ex: ExecutionContext)
extends IdentityService[User] {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    dbConfig.db.run(UserDBIO.findOne(loginInfo))

}
