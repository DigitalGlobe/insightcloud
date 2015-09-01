package com.digitalglobe.insight.vector;

import java.io.IOException;

public class VectorRestClientFactory
{
  public static VectorRestClient getCasAuthenticatedClient( ServiceProperties props )
      throws IOException
  {
    // authentication information
    String authService = props.getAuthService();
    String username = props.getUserName();
    String password = props.getPassword();

    // the base URL for accessing the vector service
    String appService = props.getAppService();

    // set up the client
    CasAuthenticatedVectorRestClient client = new CasAuthenticatedVectorRestClient();
    client.setAuthService( authService );
    client.setAppService( appService );

    System.out.println( "Authenticating with the CAS application. . . ." );
    client.authenticate( username, password );

    return client;
  }
}
