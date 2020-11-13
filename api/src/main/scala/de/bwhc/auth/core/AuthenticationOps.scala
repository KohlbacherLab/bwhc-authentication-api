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

  type AuthActionBuilder[User] = ActionBuilder[AuthReq,AnyContent]

/*
  def authService: AuthenticationService[User]


  val authActionBuilder =
    new ActionBuilder[AuthReq, AnyContent]
    {

      val parser = controllerComponents.parsers.default

      val executionContext = defaultExecutionContext

      def invokeBlock[T](
        request: Request[T],
        block: AuthReq[T] => Future[Result]
      ): Future[Result] = {

        for {
          optUser <- authService.authenticate(request)(executionContext)
          result  <- optUser match {
                       case Some(user) => block(new AuthenticatedRequest(user,request))
                       case None    => Future.successful(Unauthorized)
                     }
        } yield result
      }

    }


  def AuthenticatedAction: AuthActionBuilder[User] =
    authActionBuilder


  def AuthenticatedAction(
    authorization: Authorization[User]
  ): AuthActionBuilder[User] = 
    AuthenticatedAction andThen Require(authorization)
*/


  def AuthenticatedAction(
    implicit
    ec: ExecutionContext,
    authService: AuthenticationService[User]
  ): AuthActionBuilder[User] =
    new ActionBuilder[AuthReq, AnyContent]
    {
      val parser = controllerComponents.parsers.default

      val executionContext = ec

      def invokeBlock[T](
        request: Request[T],
        block: AuthReq[T] => Future[Result]
      ): Future[Result] = {

        for {
          optUser <- authService authenticate request 
          result  <- optUser match {
                       case Some(user) => block(new AuthenticatedRequest(user,request))
                       case None       => Future.successful(Unauthorized)
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
  ): AuthActionBuilder[User] = 
    AuthenticatedAction andThen Require(authorization)


}
