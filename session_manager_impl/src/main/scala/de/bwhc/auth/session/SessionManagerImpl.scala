package de.bwhc.auth.session



import java.time.Instant
import java.util.UUID

import scala.concurrent.{ExecutionContext,Future}

import scala.collection.concurrent.{Map,TrieMap}

import play.api.mvc.{
  ControllerHelpers,
  RequestHeader,
  Result
}

import de.bwhc.util.Logging

import de.bwhc.auth.api._

import de.bwhc.user.api.{User,Role}


class SessionManagerImplProvider extends UserSessionManagerProvider
{

  override def getInstance: UserSessionManager =
    SessionManagerImpl.instance

}


object SessionManagerImpl
{

  lazy val instance = new SessionManagerImpl

}


class SessionManagerImpl
extends UserSessionManager
with Logging
{

  import ControllerHelpers.{Ok,NotFound}

  import java.util.concurrent.{Executors,TimeUnit}


  private val BWHC_SESSION_KEY = "bwhc-session-token"


  private def newToken: Session.Token =
    Session.Token(UUID.randomUUID.toString)

  private val sessions: Map[Session.Token, Session] =
    TrieMap.empty[Session.Token, Session] 

  private val timeoutSeconds = 300


  //---------------------------------------------------------------------------
  // Scheduled clean-up task of timed-out sessions
  //---------------------------------------------------------------------------

  private class CleanupTask extends Runnable with Logging
  {

    override def run: Unit = {

      log.debug("Running clean-up task for timed out User sessions")

      val timedOutSessionIds =
        sessions.values
          .filter(_.lastRefresh isBefore Instant.now.minusSeconds(300)) // 50 min timeout limit
          .map(_.token)

      if (!timedOutSessionIds.isEmpty){
         log.info("Timed out sessions detected, removing them...")
      }

      sessions --= timedOutSessionIds

      log.debug("Finished running clean-up task for timed out sessions")

    }

  }

  private val executor = Executors.newSingleThreadScheduledExecutor

  executor.scheduleAtFixedRate(
    new CleanupTask,
    30,
    60,
    TimeUnit.SECONDS
  )


  override def login(
    userWithRoles: UserWithRoles
  )(
    implicit
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[Result] = {

    log.info(s"Logging in ${userWithRoles.userId}")

    Future.successful {

      sessions.values
        .find(session => session.userWithRoles.userId == userWithRoles.userId)
        .map(_.token)
        .tapEach(sessions remove _)

      val session = 
        Session(
          newToken,
          Instant.now,
          userWithRoles,
          Instant.now
        )

      sessions += (session.token -> session)

      Ok.addingToSession(BWHC_SESSION_KEY -> session.token.value)
   
    }

  }


  override def authenticate(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Option[UserWithRoles]] = {

    log.debug(s"Authenticating request: ${request.session.get(BWHC_SESSION_KEY)}")

    Future.successful {

      for {

        token <- request.session.get(BWHC_SESSION_KEY)

        session <- sessions.get(Session.Token(token))

        if (Instant.now isBefore session.lastRefresh.plusSeconds(timeoutSeconds))

        refreshed = session.copy(lastRefresh = Instant.now)

        _ = sessions += (refreshed.token -> refreshed)

      } yield refreshed.userWithRoles

    }

  }


  override def logout(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Result] = {

    Future.successful {

      val loggedOut =
        for {
          tkn <- request.session.get(BWHC_SESSION_KEY)

          ssn <- sessions remove Session.Token(tkn)

           _ = log.info(s"Successfully logged out ${ssn.userWithRoles.userId}")

           result = Ok.removingFromSession(BWHC_SESSION_KEY)(request)

        } yield result

      loggedOut.getOrElse(NotFound.withNewSession)

    }

  }


}
