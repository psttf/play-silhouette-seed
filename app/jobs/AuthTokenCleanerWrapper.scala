package jobs

import akka.actor.ActorRef

case class AuthTokenCleanerWrapper(underlying: ActorRef) extends AnyVal
