package de.bwhc.auth.api



import de.bwhc.user.api.{User,Role}


final case class UserWithRoles
(
  userId: User.Id,
  roles: Set[Role.Value]  
)
