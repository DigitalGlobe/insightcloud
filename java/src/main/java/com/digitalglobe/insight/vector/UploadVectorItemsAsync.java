package com.digitalglobe.insight.vector;

import com.digitalglobe.insight.vector.client.VectorRestClient;
import com.digitalglobe.insight.vector.client.VectorRestClientFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URLEncoder;

public class UploadVectorItemsAsync
{
  public static void main( String[] args ) throws Exception
  {
    if ( args.length < 1 )
    {
      throw new RuntimeException( "Configuration file must be specified." );
    }

    ServiceProperties props = new ServiceProperties( args[0] );

    // the base URL for accessing the vector service
    String appService = props.getAppService();
    String urlBase = props.getUrlBase();
    String appBase = appService + urlBase;

        // set up the client
    VectorRestClient client = VectorRestClientFactory.getClient( props );

    // Async uploads only take an array of items to upload
    System.out.println( "Sending multi-item upload. . . .");
    String index = URLEncoder.encode( "vector-test-upload-{geohash}-{item_date}", "UTF-8" );
    String multiItemUploadRequest = appBase + "/api/vectors/"
        + index + "/async";
    String multiItemJson = "[" +
      "{" +
      "    \"type\": \"Feature\"," +
      "        \"geometry\": {" +
      "            \"type\": \"Point\"," +
      "            \"coordinates\": [-42,42]" +
      "        }," +
      "        \"properties\": {" +
      "            \"name\" : \"Jabberwocky, line 1\"," +
      "            \"item_date\" : \"2015-09-18T14:16:00Z\"," +
      "            \"ingest_source\" : \"Test\"," +
      "            \"item_type\" : \"Test item\"," +
      "            \"text\" : \"'Twas brillig and the slithy toves\"," +
      "            \"source\" : \"Lewis Carroll\"," +
      "            \"attributes\" : {" +
      "               \"my.arbitrary.string\":\"foo\"," +
      "               \"my.arbitrary.number\": 42," +
      "               \"my.arbitrary.date\": \"2015-07-23T13:37:42.000Z\"" +
      "            }\n" +
      "        }\n" +
      "}," +
      "{" +
      "    \"type\": \"Feature\"," +
      "        \"geometry\": {" +
      "            \"type\": \"Point\"," +
      "            \"coordinates\": [-43,43]" +
      "        }," +
      "        \"properties\": {" +
      "            \"name\" : \"Jabberwocky, line 2\"," +
      "            \"item_date\" : \"2015-09-18T14:16:00Z\"," +
      "            \"ingest_source\" : \"Test\"," +
      "            \"item_type\" : \"Test item\"," +
      "            \"text\" : \"did gyre and gimbel in the wabe\"," +
      "            \"source\" : \"Lewis Carroll\"," +
      "            \"attributes\" : {" +
      "               \"my.arbitrary.string\":\"foo\"," +
      "               \"my.arbitrary.number\": 43," +
      "               \"my.arbitrary.date\": \"2015-07-23T13:37:42.000Z\"" +
      "            }\n" +
      "        }\n" +
      "}," +
    "]";

    String multiItemResponse = client.executePost( multiItemUploadRequest, multiItemJson );
    System.out.println( multiItemResponse );

    handleItemPaths( multiItemResponse, appBase, client );

    client.logout();
  }

  public static void handleItemPaths( String response, String appBase, VectorRestClient client ) throws IOException, InterruptedException
  {
    // the POST response gives us an array of paths to retrieve the created items
    JSONArray itemPaths = (JSONArray) JSONValue.parse( response );
    for ( Object itemPath : itemPaths )
    {
      String path = (String) itemPath;
      System.out.println( "Getting object for path: " + path );
      // b/c we're brokered through monocle-3 for now, the paths are messed up
      String url = path.replaceFirst( "/insight-vector", appBase );
      String getResponse = null;
      while ( getResponse == null )
      {
        try
        {
          getResponse = client.executeGet( url );
        }
        catch ( Exception e )
        {
          // only worry if it's not a 404 for now . . .
          // we need to wait for the items to be indexed
          if ( e instanceof HttpClientErrorException )
          {
            if ( ((HttpClientErrorException) e).getStatusCode().value() == 404 )
            {
              System.out.println( "Waiting for item to be indexed. . . .");
              Thread.sleep( 10000 );
            }
            else
            {
              throw e;
            }
          }
          else
          {
            throw e;
          }
        }
      }
      System.out.println( getResponse );

      // DELETE the items we just uploaded to clean up test items.
      // Note, only the user specified in the item's ingestAttributes{ _rest.user }
      // property can update/delete an item.
      System.out.println( "Deleting item at path: " + path );
      String deleteResponse = client.executeDelete( url );
      // should be no content
      if ( deleteResponse != null )
      {
        throw new RuntimeException( "Got got content when we shouldn't have: "
                                        + deleteResponse );
      }
      else
      {
        System.out.println( "No content from DELETE response . . . ok.");
      }
    }
  }
}
