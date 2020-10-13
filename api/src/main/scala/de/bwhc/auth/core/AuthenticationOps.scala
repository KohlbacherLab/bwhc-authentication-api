package de.bwhc.auth.core



import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  AnyContent,
  ActionBuilder,
  ActionTransformer,
  BaseController,
  Request,
  Result,
  BodyParser
}

/*

Based on examples found in Play documentation on Action Composition
 
https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition#Action-composition

combined with inspiration drawn from Silhouette

https://www.silhouette.rocks/

*/


trait AuthenticationOps[User] extends AuthorizationOps
{

  this: BaseController =>


  type AuthReq[+T] = AuthenticatedRequest[User,T]

  type UserActionBuilder[User] = ActionBuilder[AuthReq,AnyContent]



  def AuthenticatedAction(
    implicit
    ec: ExecutionContext,
    authService: AuthenticationService[User]
  ): UserActionBuilder[User] =
    new ActionBuilder[AuthReq, AnyContent]
    {
      val parser = controllerComponents.parsers.default

      val executionContext = ec

      def invokeBlock[T](
        request: Request[T],
        block: AuthReq[T] => Future[Result]
      ): Future[Result] = {

        for {
          optUser <- authService.authenticate(request)
          result  <- optUser match {
                       case Some(user) => block(new AuthenticatedRequest(user,request))
                       case None    => Future.successful(Unauthorized)
                     }
        } yield result
      }

    }


  def AuthenticatedAction(
    authorization: Authorization[User]
  )(
    implicit
    ec: ExecutionContext,
    authService: AuthenticationService[User]
  ): UserActionBuilder[User] = 
    AuthenticatedAction.andThen(Require(authorization))


}
