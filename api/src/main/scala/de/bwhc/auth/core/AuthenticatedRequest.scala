package de.bwhc.auth.core



import play.api.mvc.{
  Request,
  WrappedRequest
}


class AuthenticatedRequest[User,+T](
  val user: User,
  val request: Request[T]
)
extends WrappedRequest(request)


