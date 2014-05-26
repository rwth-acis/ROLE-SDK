package de.imc.widgetstore.test;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.cxf.helpers.IOUtils;
import java.io.InputStream;
import java.net.URL;

/**
 * JUnit tests for the Widget Store service
 **/  
public class WidgetStoreServiceDefaultTest {

  @Test
  public void testWidgetStoreService() {

    // tests for Widget Store (http://role-widgetstore.eu; Drupal)
    System.out.println("----- testing Widget Store SPARQL endpoint -----");
    String wsUrl = "http://www.role-widgetstore.eu/simplerdf/sparql";

    try {
      URL url = new URL(wsUrl);
      InputStream is = url.openStream();
      String stringResponse = IOUtils.toString(is);
//      System.out.println("Response for SPARQL endpoint of the Widget Store: " + stringResponse);
      if (stringResponse.indexOf("<title>ARC SPARQL+ Endpoint</title>") > 0) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }
    } catch (Exception x) {
System.out.println("exception: '" + x.getClass() + "'");
      // remark: SPARQL endpoint is available if there is no FileNotFoundException
      //         (surprisingly it's not a 404 error code; error 403 means that the
      //         user has to authenticate to get access... so we check for
      //         (1) unknown host, (2) file not found and (3) ???)
      if (x.getClass().toString().equals("class java.net.UnknownHostException") ||
          x.getClass().toString().equals("class java.io.FileNotFoundException")) {
        assertTrue(false);
      } else {
        assertTrue(true);
      }
    }

  }
}
