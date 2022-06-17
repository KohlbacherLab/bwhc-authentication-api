package de.bwhc.auth.core



import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  RequestHeader
}


/*

  Based on examples found in Play documentation on Action Composition:
 
  https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition#Action-composition

  combined with inspiration drawn from Silhouette:

  https://github.com/mohiva/play-silhouette

*/


trait AuthenticationService[User]{

  def authenticate(
    req: RequestHeader
  )(
    implicit ec: ExecutionContext
  ): Future[Option[User]]

}


object AuthenticationService
{

  def apply[User](
    f: RequestHeader => Option[User]
  ): AuthenticationService[User] =
    new AuthenticationService[User]{
    
      override def authenticate(
        req: RequestHeader
      )(
        implicit ec: ExecutionContext
      ): Future[Option[User]] =
        Future.successful(f(req))
    
    }

}

