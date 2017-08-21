package jobs

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
 * Schedules the jobs.
 */
class Scheduler (
  system: ActorSystem,
  authTokenCleanerWrapper: AuthTokenCleanerWrapper
) {

  lazy val authTokenCleaner = authTokenCleanerWrapper.underlying

  QuartzSchedulerExtension(system).schedule(
    "AuthTokenCleaner",
    authTokenCleaner,
    AuthTokenCleaner.Clean
  )

  authTokenCleaner ! AuthTokenCleaner.Clean

}
