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

  def hasRole(role: Role.Value): Boolean = roles contains role

  def hasAnyOf(rs: Set[Role.Value]): Boolean = !(roles & rs).isEmpty

  def hasAnyOf(r1: Role.Value, r2: Role.Value, rs: Role.Value*): Boolean =
    this.hasAnyOf((r1 +: r2 +: rs).toSet) 


  def has(
    authorization: Authorization[UserWithRoles]
  )(
    implicit ec: ExecutionContext
  ): Future[Boolean] = authorization.isAuthorized(this)


  def isAllowedTo(
    authorization: Authorization[UserWithRoles]
  )(
    implicit ec: ExecutionContext
  ): Future[Boolean] = authorization.isAuthorized(this) 

}


object UserWithRoles
{
  implicit val format = Json.format[UserWithRoles]
}
