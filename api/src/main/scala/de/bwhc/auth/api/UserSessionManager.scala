package de.bwhc.auth.api


import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc.{RequestHeader,Result}

import play.api.libs.json.Writes

import de.bwhc.util.spi._

import de.bwhc.user.api.User

import de.bwhc.auth.core.AuthenticationService



trait UserSessionManagerProvider extends SPI[UserSessionManager]


object UserSessionManager extends SPILoader(classOf[UserSessionManagerProvider])


trait UserSessionManager extends AuthenticationService[UserWithRoles]
{

  def login(
    userWithRoles: UserWithRoles,
  )(
    implicit
    ec: ExecutionContext
  ): Future[Result]


  def logout(
    request: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Result]

}
