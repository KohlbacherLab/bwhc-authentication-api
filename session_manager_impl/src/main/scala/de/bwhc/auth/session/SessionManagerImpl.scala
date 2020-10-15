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


  private val BWHC_SESSION_KEY = "bwhc-session-token"


  private def newToken: Session.Token =
    Session.Token(UUID.randomUUID.toString)

  private val sessions: Map[Session.Token, Session] =
    TrieMap.empty[Session.Token, Session] 

  private val timeoutSeconds = 300


  //TODO: Clean-up task for timed-out sessions



  def login(
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
        .tapEach(sessions -= _)

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


  def authenticate(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Option[UserWithRoles]] = {

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



  def logout(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Result] = {

    Future.successful {

      val loggedOut =
        for {
          tkn <- request.session.get(BWHC_SESSION_KEY)

          ssn <- sessions.remove(Session.Token(tkn))

           _ = log.info(s"Successfully logged out ${ssn.userWithRoles.userId}")

           result = Ok.removingFromSession(BWHC_SESSION_KEY)(request)

        } yield result

      loggedOut.getOrElse(NotFound.withNewSession)

/*       
      request.session
        .get(BWHC_SESSION_KEY)
        .tapEach(
          tkn => sessions -= Session.Token(tkn)
        )
        .headOption
        .map(tkn => Ok.withNewSession)
        .getOrElse(Ok.withNewSession)
*/       
    }

  }


}
