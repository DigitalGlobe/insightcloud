package com.digitalglobe.insight.vector.client;

import com.digitalglobe.insight.vector.ServiceProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

public class OAuth2VectorRestClient implements VectorRestClient
{

  private OAuth2RestTemplate restTemplate = null;

  public OAuth2VectorRestClient( ServiceProperties props )
  {
    String tokenUrl = props.getAuthService();
    String clientId = props.getProperty( "clientId" );
    String clientSecret = props.getProperty( "clientSecret" );
    String username = props.getUserName();
    String password = props.getPassword();

    if ( tokenUrl == null || clientId == null || clientSecret == null ||
                             username == null || password == null )
    {
      throw new RuntimeException( "tokenUrl, clientId, clientSecret, username, password must all be provided in the service properties." );
    }

    ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
    resource.setAccessTokenUri( tokenUrl );
    resource.setClientId( clientId );
    resource.setClientSecret( clientSecret );
    resource.setUsername( username );
    resource.setPassword( password );
    this.restTemplate = new OAuth2RestTemplate( resource );
  }

  @Override
  public String executeGet( String url ) throws IOException
  {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity( url, String.class );
    String response = responseEntity.getBody();
    if ( responseEntity.getStatusCode().value() == 200 )
    {
      return response;
    }
    else
    {
      throw new IOException("service access error: statusCode="
                                + responseEntity.getStatusCode().value()
                                + "; response=" + response);
    }
  }

  @Override
  public String executePost( String url, String body ) throws IOException
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType( MediaType.APPLICATION_JSON );

    HttpEntity<String> entity = new HttpEntity<>( body, headers );
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                                                    url, entity, String.class );

    if ( responseEntity.getStatusCode().value() != 200
        || responseEntity.getStatusCode().value() != 201 )
    {
      return responseEntity.getBody();
    }
    else
    {
      throw new IOException("service access error: statusCode="
                                + responseEntity.getStatusCode().value()
                                + "; response=" + responseEntity.getBody() );
    }
  }

  @Override
  public String executeDelete( String url ) throws IOException
  {
    restTemplate.delete( url );
    return null;
  }

  @Override
  public VectorPagingResponse executePagingRequest( String url, Map<String, String> params ) throws IOException
  {
    MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
    for ( Map.Entry<String,String> entry : params.entrySet() )
    {
      map.add( entry.getKey(), entry.getValue() );
    }
    ResponseEntity<String> responseEntity = restTemplate.postForEntity( url, map, String.class );
    String response = responseEntity.getBody();
    if ( responseEntity.getStatusCode().value() == 200 )
    {
      int itemCount = Integer.parseInt(
          responseEntity.getHeaders().getFirst( "Vector-Item-Count" ) );
      System.out.println( "Item count: " + itemCount );
      String pagingId = responseEntity.getHeaders().getFirst( "Vector-Paging-Id" );
      System.out.println( "Paging ID: " + pagingId );
      return new VectorPagingResponse( responseEntity.getStatusCode().value(),
                                       response, itemCount, pagingId );
    }
    else
    {
      throw new IOException("service access error: statusCode="
                                + responseEntity.getStatusCode().value()
                                + "; response=" + response);
    }
  }

  @Override
  public void logout()
  {
    //TODO: Implement this method.
  }
}
