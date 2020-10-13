package de.bwhc.auth.core


import javax.inject.Inject

import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  Request,
  WrappedRequest
}


class AuthenticatedRequest[User,+T](
  val user: User,
  val request: Request[T]
)
extends WrappedRequest(request)


