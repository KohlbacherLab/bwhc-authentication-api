package de.bwhc.auth.session



import java.time.Instant
import java.util.UUID

import scala.concurrent.{ExecutionContext,Future}

import scala.collection.concurrent.{Map,TrieMap}

import play.api.mvc.{
  ControllerHelpers,
  Cookie,
  DiscardingCookie,
  RequestHeader,
  Result
}

import play.api.libs.json.{Json,Writes}

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

  import ControllerHelpers._

  import java.util.concurrent.{Executors,TimeUnit}


  private def newToken: AccessToken =
    AccessToken(UUID.randomUUID.toString)

  private val sessions: Map[AccessToken, Session] =
    TrieMap.empty[AccessToken, Session] 

  private val timeoutDuration = 3600    // 1 h timeout limit

  private val maxDuration     = 24*3600 // 24 h timeout limit


  //---------------------------------------------------------------------------
  // Scheduled clean-up task of timed-out sessions
  //---------------------------------------------------------------------------

  private class CleanupTask extends Runnable with Logging
  {

    override def run: Unit = {

      log.debug("Running clean-up task for timed out User sessions")

      val timedOutSessionIds =
        sessions.values
          .filter {
            s =>
              val now = Instant.now
              (s.lastRefresh isBefore (now minusSeconds timeoutDuration)) ||
                (s.createdAt isBefore (now minusSeconds maxDuration))
          }
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
    userWithRoles: UserWithRoles,
  )(
    implicit
    ec: ExecutionContext
  ): Future[Result] = {

    log.info(s"Logging in ${userWithRoles.userId}")

    Future.successful {

      val prevSession =
        sessions.values
          .find(session => session.userWithRoles.userId == userWithRoles.userId)

      prevSession match {

        case None => {

          val session = 
            Session(
              newToken,
              Instant.now,
              userWithRoles,
              Instant.now
            )
          
          sessions += (session.token -> session)
          
          val oauthToken =
            OAuthToken(
              session.token,
              TokenType.Bearer,
              timeoutDuration,
              None,
              session.createdAt,
              Some("bwhc")
            )
          
          Ok(Json.toJson(oauthToken))
        }

        case Some(_) =>
          Forbidden("Already logged in!")

      }

    }

  }


  override def authenticate(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Option[UserWithRoles]] = {

    val authorization = request.headers.get(AUTHORIZATION)

    log.debug(s"Authenticating request: ${authorization}")

    Future.successful {

      for {

        token <- authorization
                   .filter(_.startsWith("Bearer "))
                   .map(_.split(" ")(1))
                   .map(AccessToken(_))

        session <- sessions.get(token)

        if (Instant.now isBefore session.lastRefresh.plusSeconds(timeoutDuration))

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

          token <- request.headers.get(AUTHORIZATION)
                   .filter(_.startsWith("Bearer "))
                   .map(_.split(" ")(1))
                   .map(AccessToken(_))

          ssn <- sessions remove token

           _ = log.info(s"Successfully logged out ${ssn.userWithRoles.userId}")

           result = Ok

        } yield result

      loggedOut.getOrElse(NotFound)

    }

  }


}
