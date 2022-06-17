package de.bwhc.auth.core



import play.api.mvc.{
  Request,
  WrappedRequest
}


/*

  Based on examples found in Play documentation on Action Composition:
 
  https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition#Action-composition

  combined with inspiration drawn from Silhouette:

  https://github.com/mohiva/play-silhouette

*/

class AuthenticatedRequest[User,+T](
  val user: User,
  val request: Request[T]
)
extends WrappedRequest(request)


