package de.bwhc.auth.fake


import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  ControllerHelpers,
  RequestHeader,
  Result
}

import de.bwhc.util.Logging

import de.bwhc.auth.api._

import de.bwhc.user.api.{User,Role}


class FakeSessionManagerProvider extends UserSessionManagerProvider
{

  override def getInstance: UserSessionManager =
    FakeSessionManager

}


object FakeSessionManager
extends UserSessionManager
with Logging
{

  private val user =
    UserWithRoles(
      User.Id("fake-user-id"),
      Role.values.toSet
    )


  def authenticate(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Option[UserWithRoles]] = {

    log.warn("FakeSessionManager only meant for temporary testing purposes. Replace it with a real implementation in production!")

    Future.successful(Some(user))
  }


  def login(
    userWithRoles: UserWithRoles
  )(
    implicit ec: ExecutionContext
  ): Future[Result] = {

    log.warn("FakeSessionManager only meant for temporary testing purposes. Replace it with a real implementation in production!")

    Future.successful(ControllerHelpers.Ok)
  }


  def logout(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Result] = {

    log.warn("FakeSessionManager only meant for temporary testing purposes. Replace it with a real implementation in production!")

    Future.successful(ControllerHelpers.Ok)
  }


}
