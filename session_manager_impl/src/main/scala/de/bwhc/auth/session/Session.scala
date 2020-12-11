package de.bwhc.auth.session


import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json

import de.bwhc.auth.api.UserWithRoles



final case class Session
(
  token: AccessToken,
  createdAt: Instant,
  userWithRoles: UserWithRoles,
  lastRefresh: Instant
)

object Session
{
  implicit val format = Json.format[Session]
}
