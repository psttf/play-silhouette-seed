package infrastructure

import play.api.db.slick.{DbName, SlickComponents}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DatabaseComponents extends SlickComponents {
  lazy val dbConfig: DatabaseConfig[JdbcProfile] =
    slickApi.dbConfig(DbName("default"))
}
