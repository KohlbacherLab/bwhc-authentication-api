package de.bwhc.auth.api



import scala.concurrent.{ExecutionContext,Future}

import de.bwhc.user.api.User

import de.bwhc.auth.core.Authorization


object Authorizations
{

  import de.bwhc.user.api.Role._

  import Authorization._


  val AdminRights =
    Authorization[UserWithRoles](_ is Admin)


  val DataQualityAccessRights =
    Authorization[UserWithRoles](_ is Documentarist)


  val LocalQCAccessRights =
    Authorization[UserWithRoles](user =>
      (user is LocalZPMCoordinator) ||
      (user is MTBCoordinator)
    )


  val GlobalQCAccessRights =
    Authorization[UserWithRoles](_ is GlobalZPMCoordinator)


  val LocalEvidenceQueryRights =
    Authorization[UserWithRoles](user =>
      (user is LocalZPMCoordinator) ||
      (user is MTBCoordinator) ||
      (user is Researcher)
    )


  def FederatedEvidenceQueryRights(implicit ec: ExecutionContext) =
    LocalEvidenceQueryRights or Authorization(_ is GlobalZPMCoordinator)


  val EvidenceQueryRights = LocalEvidenceQueryRights



  def ResourceOwnership[Id](
    id: Id
  )(
    implicit
    ec: ExecutionContext,
    isOwner: (User.Id,Id) => Future[Boolean]
  ): Authorization[UserWithRoles] =
    Authorization.async {
      case UserWithRoles(user,_) => isOwner(user,id)
    }


}

