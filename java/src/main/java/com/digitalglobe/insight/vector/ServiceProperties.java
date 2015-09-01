package com.digitalglobe.insight.vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServiceProperties
{
  private Properties props;

  public ServiceProperties( String propsFile ) throws IOException
  {
    Properties props = loadProperties( propsFile );
    validateServiceProperties( props );
    this.props = props;
  }

  public ServiceProperties( Properties props )
  {
    validateServiceProperties( props );
    this.props = props;
  }

  public String getProperty( String key )
  {
    return props.getProperty( key );
  }

  public String getAuthService()
  {
    return props.getProperty( "authService" );
  }

  public String getAppService()
  {
    return props.getProperty( "appService" );
  }

  public String getUserName()
  {
    return props.getProperty( "username" );
  }
  public String getPassword()
  {
    return props.getProperty( "password" );
  }

  public String getUrlBase()
  {
    return props.getProperty( "urlBase", "" );
  }

  public Properties loadProperties ( String propsFile ) throws IOException
  {
    Properties props = new Properties();
    FileInputStream fIn = null;
    try
    {
      fIn = new FileInputStream( new File( propsFile ) );
      props.load( fIn );
    }
    finally
    {
      if ( fIn != null )
      {
        try { fIn.close(); }
        catch ( Exception e ) { /* do nothing */ }
      }
    }
    return props;
  }

  public boolean validateServiceProperties( Properties props )
  {
    if ( ! props.containsKey( "authService" ) )
    {
      throw new RuntimeException( "Auth service must be configured." );
    }
    if ( ! props.containsKey( "appService" ) )
    {
      throw new RuntimeException( "App service must be configured." );
    }
    if ( ! props.containsKey( "username" ) )
    {
      throw new RuntimeException( "User name must be configured." );
    }
    if ( ! props.containsKey( "password" ) )
    {
      throw new RuntimeException( "Password must be configured." );
    }
    return true;
  }
}
