package de.bwhc.auth.fake



import org.scalatest.flatspec.AsyncFlatSpec


import de.bwhc.auth.api.UserSessionManager



class Tests extends AsyncFlatSpec
{


  "FakeSessionManagerProvider" must "successfully load UserSessionManager instance" in {

     val sessionManager = UserSessionManager.getInstance

     assert(sessionManager.isSuccess)

  }


}
