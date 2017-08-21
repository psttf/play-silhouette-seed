package silhouetteIntegration

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import data.UserDBIO
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PasswordInfoDAO(dbConfig: DatabaseConfig[JdbcProfile])
extends DelegableAuthInfoDAO[PasswordInfo] {

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    dbConfig.db.run(UserDBIO.findOne(loginInfo)) map
      ( _ map ( _.passwordInfo ) )

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    update(loginInfo, authInfo)

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    dbConfig.db.run(UserDBIO.update( loginInfo, authInfo ))
      .map { _ => authInfo }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    update(loginInfo, authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???

}
