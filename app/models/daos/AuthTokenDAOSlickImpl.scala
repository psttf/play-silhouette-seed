package models.daos

import java.util.UUID

import data.AuthTokenDBIO
import models.AuthToken
import org.joda.time.DateTime
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOSlickImpl(dbConfig: DatabaseConfig[JdbcProfile]) {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) =
    dbConfig.db.run(AuthTokenDBIO.lookup(id))

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: DateTime) =
    dbConfig.db.run(AuthTokenDBIO.findExpired(dateTime))

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken) =
    dbConfig.db.run(AuthTokenDBIO.save(token))

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID) =
    dbConfig.db.run(AuthTokenDBIO.remove(id))

}
