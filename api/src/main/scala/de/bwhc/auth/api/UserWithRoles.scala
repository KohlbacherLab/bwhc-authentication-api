package de.bwhc.auth.api



import scala.concurrent.{ExecutionContext,Future}

import play.api.libs.json.Json

import de.bwhc.user.api.{User,Role}

import de.bwhc.auth.core.Authorization


final case class UserWithRoles
(
  userId: User.Id,
  roles: Set[Role.Value]  
)
{

  def is(role: Role.Value) = roles contains role

  def has(
    authorization: Authorization[UserWithRoles]
  )(
    implicit ec: ExecutionContext
  ): Future[Boolean] = authorization isAuthorized this 

}


object UserWithRoles
{
  implicit val format = Json.format[UserWithRoles]
}
