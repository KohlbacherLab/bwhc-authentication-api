package de.bwhc.auth.core


import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  ActionFilter,
  ControllerHelpers,
  Result
}


/*

  Based on examples found in Play documentation on Action Composition:
 
  https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition#Action-composition

  combined with inspiration drawn from Silhouette:

  https://github.com/mohiva/play-silhouette

*/


trait Authorization[User]
{
  def isAuthorized(
    user: User
  )(
    implicit ec: ExecutionContext
  ): Future[Boolean]
  
}


object Authorization
{

  implicit class LogicalOperatorSyntax[User](val a: Authorization[User]) extends AnyVal
  {

    def and(b: Authorization[User]): Authorization[User] =
      new Authorization[User]{
        override def isAuthorized(user: User)(implicit ec: ExecutionContext) =
          a.isAuthorized(user)
            .flatMap {
              // AND logic:
              // if first authorization check already fails, short-circuit to 'false'
              // else result depends on second auth check
              case false => Future.successful(false)
              case true  => b.isAuthorized(user)
            }
      }


    def or(b: Authorization[User]) =
      new Authorization[User]{
        override def isAuthorized(user: User)(implicit ec: ExecutionContext) =
          a.isAuthorized(user)
           .flatMap {
             // OR logic:
             // if first authorization check already succeeds, short-circuit to 'true'
             // else result depends on second auth check
             case true  => Future.successful(true)
             case false => b.isAuthorized(user)
           }
      }


    def &&(b: Authorization[User]) = a and b

    def ||(b: Authorization[User]) = a or b

    def AND(b: Authorization[User]) = a and b

    def OR(b: Authorization[User]) = a or b
 
  }


  def apply[User](f: User => Boolean): Authorization[User] =
    new Authorization[User]{
      def isAuthorized(user: User)(implicit ec: ExecutionContext) =
        Future.successful(f(user))
    }


  def async[User](f: User => Future[Boolean]): Authorization[User] =
    new Authorization[User]{
      def isAuthorized(user: User)(implicit ec: ExecutionContext) =
        f(user)
    }
}


trait AuthorizationOps
{

  import ControllerHelpers.{Unauthorized, Forbidden}


  def Require[User](
    auth: Authorization[User]
  )(
    implicit ec: ExecutionContext
  ) = new ActionFilter[({ type Req[T] = AuthenticatedRequest[User,T] })#Req]{

    val executionContext = ec

    def filter[T](request: AuthenticatedRequest[User,T]): Future[Option[Result]] = {
      for { 
        allowed   <- auth isAuthorized request.user
        objection = if (allowed) None else Some(Forbidden)
      } yield objection

    }

  }


}
