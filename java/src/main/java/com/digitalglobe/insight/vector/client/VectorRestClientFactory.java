package com.digitalglobe.insight.vector.client;

import com.digitalglobe.insight.vector.ServiceProperties;

import java.io.IOException;

public class VectorRestClientFactory
{
  public static VectorRestClient getClient( ServiceProperties props )
      throws IOException
  {
    if ( props.getAuthType().equals( ServiceProperties.AuthType.CAS ) )
    {
      return getCasAuthenticatedClient( props );
    }
    return getOAuth2AuthenticatedClient( props );
  }

  public static VectorRestClient getOAuth2AuthenticatedClient( ServiceProperties props )
  {
    return new OAuth2VectorRestClient( props );
  }

  public static VectorRestClient getCasAuthenticatedClient( ServiceProperties props )
      throws IOException
  {
    return new CasAuthenticatedVectorRestClient( props );
  }
}
