package de.bwhc.auth.session



import java.time.Instant

import play.api.libs.json.Json



final case class AccessToken(value: String) extends AnyVal
object AccessToken
{
  implicit val format = Json.valueFormat[AccessToken]
}

final case class RefreshToken(value: String) extends AnyVal
object RefreshToken
{
  implicit val format = Json.valueFormat[RefreshToken]
}


object TokenType extends Enumeration
{
  val Bearer = Value

  implicit val format = Json.formatEnum(this)
}


final case class OAuthToken
(
  access_token: AccessToken,
  token_type: TokenType.Value,
  expires_in: Int,
  refresh_token: Option[RefreshToken],
  created_at: Long,
  scope: Option[String]
)

object OAuthToken
{

  def apply(
    accessToken: AccessToken,
    tokenType: TokenType.Value,
    expiresIn: Int,
    refreshToken: Option[String],
    createdAt: Instant,
    scope: Option[String] = None
  ): OAuthToken =
    OAuthToken(
      accessToken,
      tokenType,
      expiresIn,
      refreshToken.map(RefreshToken(_)),
      createdAt.toEpochMilli,
      scope
    )


  implicit val format = Json.format[OAuthToken]

}
