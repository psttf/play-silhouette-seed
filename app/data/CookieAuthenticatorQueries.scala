package data

import slick.jdbc.PostgresProfile.api._

object CookieAuthenticatorQueries {

  val table = TableQuery[CookieAuthenticatorTable]

  val schema = table.schema

  val byId = Compiled( (id: Rep[String]) =>
    table.filter(_.id === id)
  )

}
