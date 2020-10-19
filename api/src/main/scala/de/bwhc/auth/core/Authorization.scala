package de.bwhc.auth.core


import scala.concurrent.{ExecutionContext,Future}

import play.api.mvc.{
  ActionFilter,
  ControllerHelpers,
  Result
}



trait Authorization[User]
{
  def isAuthorized(user: User): Future[Boolean]
}


object Authorization
{

  implicit class LogicalOperatorSyntax[User](val a: Authorization[User])(
    implicit ec: ExecutionContext
  ){

    def &&(b: Authorization[User]) = new Authorization[User]{
      def isAuthorized(user: User) =
        for {
          okA <- a.isAuthorized(user)
          okB <- b.isAuthorized(user)
        } yield okA && okB
    }

    def ||(b: Authorization[User]) = new Authorization[User]{
      def isAuthorized(user: User) =
        for {
          okA <- a.isAuthorized(user)
          result <- if (okA) Future.successful(okA)
                    else b.isAuthorized(user)
        } yield result
    }

    def and(b: Authorization[User]) = a && b

    def or(b: Authorization[User]) = a || b

    def AND(b: Authorization[User]) = a && b

    def OR(b: Authorization[User]) = a || b

  }


  def apply[User](f: User => Boolean): Authorization[User] =
    new Authorization[User]{
      def isAuthorized(user: User) = Future.successful(f(user))
    }

  def async[User](f: User => Future[Boolean]): Authorization[User] =
    new Authorization[User]{
      def isAuthorized(user: User) = f(user)
    }


//  implicit def fromPredicate[User](f: User => Boolean): Authorization[User] =
//    apply(f)


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
