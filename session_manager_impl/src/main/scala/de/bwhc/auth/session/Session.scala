package de.bwhc.auth.session


import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json

import de.bwhc.auth.api.UserWithRoles



final case class Session
(
  token: AccessToken,
//  token: Session.Token,
  createdAt: Instant,
  userWithRoles: UserWithRoles,
  lastRefresh: Instant
)

object Session
{

//  final case class Token(value: String) extends AnyVal

//  implicit val formatToken = Json.valueFormat[Token]

  implicit val format = Json.format[Session]

}
