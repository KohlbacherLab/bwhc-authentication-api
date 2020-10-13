package de.bwhc.auth.core



import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  RequestHeader
}



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

