package de.fit.caple.cam.service.test;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.cxf.helpers.IOUtils;

import java.io.InputStream;
import java.net.URL;
  
public class CamWebServiceSJTUStatusTest {

	@Test
    public void testCamServiceStatus() {
	
		String wsUrl = "http://giotto.informatik.rwth-aachen.de/role-live/CamWebServiceSJTU/";
		String path = "CamService/status";
		
		boolean liveTablesExist = false;
		boolean testTablesExist = false;
		
		try {
            URL url = new URL(wsUrl + path);
            InputStream is = url.openStream();
            String stringResponse = IOUtils.toString(is);
            if (stringResponse.indexOf("LIVE DATABASE\nALL TABLES ARE EXISTING") > -1) {
            	liveTablesExist = true;
            }
            if (stringResponse.indexOf("TEST DATABASE\nALL TABLES ARE EXISTING") > -1) {
            	testTablesExist = true;
            }
            if (liveTablesExist && testTablesExist)
            	assertTrue(true);
            else
            	assertTrue(false);
		}
		catch (Exception x) {
			x.printStackTrace();
			assertTrue(false);
		}
    }
}
