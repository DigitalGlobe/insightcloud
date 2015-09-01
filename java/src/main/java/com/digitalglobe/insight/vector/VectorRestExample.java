package com.digitalglobe.insight.vector;

import com.digitalglobe.insight.vector.client.VectorRestClient;
import com.digitalglobe.insight.vector.client.VectorRestClientFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

public class VectorRestExample
{
  /**
   * Authenticates with the UVI, requests pages of items, executes another
   * requests for non-paged items, then logs out from the application.
   *
   * @param args no args needed at this time
   */
  public static void main(String[] args) throws Exception
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

    // Set up a bounding box to search
    String bboxParams = "left=-145.936399490872&upper=51.6307842812699"
                        + "&right=-33.6258400156824&lower=19.2701146019781";

    // assume we already know from previous requests that we want items from the
    // "Gazeteer" (sic) source, with an "item_type" of "Airport", and which are
    // a Point geometry

    System.out.println( "Retrieving 'Point' items of type 'Airport' "
                            + "from the 'Gazeteer' (sic) source." );

    // initiate a paging request
    String pagingRequest = appBase
        + "/api/esri/Gazeteer/Point/Airport/paging"
        + "?" + bboxParams
        + "&ttl=1m&count=100";

    // the paging request response should be JSON with a single field "pagingId"
    // that pagingId is used in a follow-on request to retrieve the first page of items
    String jsonString = client.executeGet( pagingRequest );
    JSONObject json = (JSONObject) JSONValue.parse( jsonString );
    String pageId = (String) json.get( "pagingId" );

    // iterate through the pages, returning items in ESRI JSON format
    String singlePageRequest = appBase + "/api/esri/paging";
    boolean doRequest = true;
    int itemTotal = 0;
    while ( doRequest )
    {
      Map<String,String> params = new HashMap<>();
      params.put( "ttl", "5m" );
      params.put( "fields", "attributes" );
      params.put( "pagingId", pageId );

      VectorRestClient.VectorPagingResponse response = client.executePagingRequest( singlePageRequest, params );
      System.out.println( "Page item count: " + response.getItemCount() );
      itemTotal += response.getItemCount();
      System.out.println( "Total so far: " + itemTotal );
      pageId = response.getPageId();

      // if we needed to, we could parse the JSON and do something with the items
      // JSONObject itemJson = (JSONObject) JSONValue.parse( response.getBody() );

      // when a paging request returns 0 items, there are no more items to return
      doRequest = ( response.getItemCount() > 0 );
    }

    // ========================================================================
    // NOTE: you can make multiple calls using the same client instance. . . .

    System.out.println( "Querying for type 'School' matching the keyword "
                        + "'technical' in the same bounding box. . . .");

    // Query for items with text matching "technical" in the same bounding box
    // returning 500 items in GeoJSON format with only the name and geometry
    // returned for each item.
    String queryRequest = appBase
        + "/api/vectors/query/items"
        + "?" + bboxParams + "&q=technical AND college&count=500";

    // when we get a collection of items it's a JSON array
    String itemsJson = client.executeGet( queryRequest );
    System.out.println( itemsJson );
    JSONArray items = (JSONArray) JSONValue.parse( itemsJson );
    System.out.println( "Returned " + items.size() + " items." );

    for ( Object item : items )
    {
      JSONObject i = (JSONObject) item;
      System.out.println( "----------------------------------------------");
      // Get the item ID and print it
      String id = ( (JSONObject) i.get( "properties" ) ).get( "id" ).toString();
      System.out.println( "ID: " + id );

      // Get the item geometry and print it
      JSONObject geom = (JSONObject) i.get( "geometry" );
      System.out.println( "Geometry: " + geom );

      // Get the item attributes and print them
      System.out.println( "Attributes:" );
      JSONObject itemAttributes = (JSONObject) ( (JSONObject) i.get( "properties" ) ).get( "attributes" );
      for ( Object key : itemAttributes.keySet() )
      {
        System.out.println( "\t" + key + ": " + itemAttributes.get( key ) );
      }
    }

    // logout from CAS
    client.logout();
  }
}
