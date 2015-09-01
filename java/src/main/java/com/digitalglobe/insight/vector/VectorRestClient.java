package com.digitalglobe.insight.vector;

import java.io.IOException;
import java.util.Map;

public interface VectorRestClient
{
  void setAppService( String appService );

  void setAuthService( String authService );

  String executeGet( String url ) throws IOException;

  String executePost( String url, String body ) throws IOException;

  String executeDelete( String url ) throws IOException;

  VectorPagingResponse executePagingRequest( String url, Map<String, String> params ) throws IOException;

  void logout();

  void authenticate( String username, String password ) throws IOException;

  public static class VectorPagingResponse
  {
    private int status;
    private String body;
    private String pageId;
    private int itemCount;

    public VectorPagingResponse( int status, String body, int itemCount, String pageId )
    {
      this.body = body;
      this.itemCount = itemCount;
      this.pageId = pageId;
      this.status = status;
    }

    public String getBody()
    {
      return body;
    }

    public int getItemCount()
    {
      return itemCount;
    }

    public String getPageId()
    {
      return pageId;
    }

    public int getStatus()
    {
      return status;
    }
  }
}
