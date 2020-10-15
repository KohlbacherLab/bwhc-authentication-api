package de.bwhc.auth.session



import org.scalatest.flatspec.AsyncFlatSpec


import de.bwhc.auth.api.UserSessionManager



class Tests extends AsyncFlatSpec
{


  "SessionManagerProvider" must "successfully load UserSessionManager instance" in {

     val sessionManager = UserSessionManager.getInstance

     assert(sessionManager.isSuccess)

  }


}
