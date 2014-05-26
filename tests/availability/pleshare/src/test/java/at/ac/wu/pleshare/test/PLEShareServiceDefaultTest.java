package at.ac.wu.pleshare.test;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.cxf.helpers.IOUtils;
import java.io.InputStream;
import java.net.URL;

/**
 * JUnit tests for the PLEShare repository services (v1, v2)
 **/  
public class PLEShareServiceDefaultTest {

  @Test
  public void testPLEShareService() {

    // tests for PLEShare v1 (teldev; OpenACS)
    System.out.println("----- testing PLEShare v1 -----");
    String wsUrl = "http://teldev.wu-wien.ac.at/pleshare/api/";
    String path;

    // test API method "Repository/information"
    path = "Repository/information";
    try {
      URL url = new URL(wsUrl + path);
      InputStream is = url.openStream();
      String stringResponse = IOUtils.toString(is);
      System.out.println("Response for 'Repository/information': " + stringResponse);
      if (stringResponse.equals("<ples><repository><id>13992</id><title>pleshare</title><url>http://teldev.wu-wien.ac.at/pleshare/</url><loginurl>http://teldev.wu-wien.ac.at/register</loginurl></repository></ples>")) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }
    } catch (Exception x) {
      x.printStackTrace();
      assertTrue(false);
    }

    // test API method "Repository/statistics"
    path = "Repository/statistics";
    try {
      URL url = new URL(wsUrl + path);
      InputStream is = url.openStream();
      String stringResponse = IOUtils.toString(is);
      System.out.println("Response for 'Repository/statistics': " + stringResponse);
      if (stringResponse.equals("<ples><statistics><metric1>1.0</metric1><metric2>1.5</metric2></statistics></ples>")) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }
    } catch (Exception x) {
      x.printStackTrace();
      assertTrue(false);
    }


    // tests for PLEShare v2 (augur; Drupal 6)
    System.out.println("----- testing PLEShare v2 -----");
    wsUrl = "http://jedi.wu.ac.at/pleshare2/pleshare_api/";
    try {
      URL url = new URL(wsUrl);
      InputStream is = url.openStream();
      String stringResponse = IOUtils.toString(is);
      System.out.println("Response for the API endpoint: " + stringResponse);
      if (stringResponse.equals("") || stringResponse.equals("\n")) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }
    } catch (Exception x) {
      x.printStackTrace();
      assertTrue(false);
    }


/*
// example for a POST request
    // test API method "user/login"
    path = "user/login?username=admin&password=<somepwd>";

    try {
      // Construct data
      String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode("roletest", "UTF-8");
      data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode("42role42", "UTF-8");
      // Send data
      URL url = new URL(wsUrl + path);
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();
      // Get the response
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      boolean is_available = false;
      while ((line = rd.readLine()) != null) {
        if (line.equals("Services Endpoint \"pleshare_api\" has been setup successfully.")) {
          is_available = true;
        }
      }
      wr.close();
      rd.close();
      if (is_available) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }
    } catch (Exception e) {
      assertTrue(false);
    }
*/

  }
}
