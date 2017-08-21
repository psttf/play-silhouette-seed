package jobs

import akka.actor._
import com.mohiva.play.silhouette.api.util.Clock
import data.AuthTokenDBIO
import jobs.AuthTokenCleaner.Clean
import org.joda.time.DateTimeZone
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthTokenCleaner (
  dbConfig: DatabaseConfig[JdbcProfile],
  clock: Clock
) extends Actor with ActorLogging {

  def receive: Receive = {
    case Clean =>
      val start = clock.now.getMillis
      val msg = new StringBuffer("\n")
      msg.append("=================================\n")
      msg.append("Start to cleanup auth tokens\n")
      msg.append("=================================\n")
      clean.map { deleted =>
        val seconds = (clock.now.getMillis - start) / 1000
        msg.append("Total of %s auth tokens(s) were deleted in %s seconds".format(deleted.length, seconds)).append("\n")
        msg.append("=================================\n")

        msg.append("=================================\n")
        log.info(msg.toString)
      }.recover {
        case e =>
          msg.append("Couldn't cleanup auth tokens because of unexpected error\n")
          msg.append("=================================\n")
          log.error(msg.toString, e)
      }
  }

  def clean =
    dbConfig.db run
      AuthTokenDBIO.findExpired(clock.now.withZone(DateTimeZone.UTC))  flatMap (
        tokens =>
          Future.sequence(tokens map ( token =>
            dbConfig.db.run(AuthTokenDBIO.remove(token.id)).map(_ => token)))
      )

}

object AuthTokenCleaner {
  case object Clean
}
