package de.bwhc.auth.api



import scala.concurrent.{ExecutionContext,Future}

import de.bwhc.user.api.User

import de.bwhc.auth.core.Authorization


object Authorizations
{

  import de.bwhc.user.api.Role._


  val AdminRights =
    Authorization[UserWithRoles](_.roles contains Admin)


  val DataQualityModeAccess =
    Authorization[UserWithRoles]{
      case UserWithRoles(_,roles) =>
        (roles contains Admin) || (roles contains Documentarist)
    }


  def ResourceOwnership[Id](
    id: Id
  )(
    implicit
    ec: ExecutionContext,
    checkOwner: (User.Id,Id) => Future[Boolean]
  ): Authorization[UserWithRoles] =
    Authorization.async { case UserWithRoles(user,_) => checkOwner(user,id) }


}
