// NOTE: THESE ARE NOT REALLY UNIT TESTS, WILL BE REMOVED IN FUTURE COMMIT AFTER MOVING OVER COMPLETELY TO photocol-cli

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import photocol.Photocol;
import spark.Spark;

import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PhotocolTest extends TestCase {

    public void testPhotocol() { }

//    public PhotocolTest() {
//        // start webserver and wait for it to load
//        Photocol.main(new String[]{});
//        Spark.awaitInitialization();
//    }
//
//    // for sending test requests to the webserver; helpful guide to managing HTTP requests from Java: (w/ cookies):
//    // https://www.baeldung.com/java-http-request; this imitates a browser session by maintaining cookies
//    private CookieManager cookieManager = new CookieManager();
//
//    private int request(String uri, String method, String data, List<String[]> headers) throws Exception {
//        String url = "http://localhost:" + Spark.port() + uri;
//        System.out.printf("Testing %s request to %s with data \"%s\".%n", url, method, data);
//        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
//        con.setRequestMethod(method);
//        con.setRequestProperty("Cookie", StringUtils.join(cookieManager.getCookieStore().getCookies(), ";"));
//
//        // write all headers
//        for (String[] header : headers)
//            con.setRequestProperty(header[0], header[1]);
//
//        if (method.equals("POST")) {
//            con.setRequestProperty("Content-Type", "application/json");
//            if (data.length() > 0) {
//                con.setDoOutput(true);
//                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
//                out.write(data);
//                out.close();
//            }
//        } else if (method.equals("PUT")) {
//            con.setDoOutput(true);
//            InputStream in = new BufferedInputStream(new FileInputStream(data));
//            OutputStream out = new BufferedOutputStream(con.getOutputStream());
//            byte[] buffer = new byte[4096];
//            int n;
//            while ((n = in.read(buffer)) > -1)
//                out.write(buffer, 0, n);
//            in.close();
//            out.close();
//        }
//
//        // send request and get status
//        int status = con.getResponseCode();
//        if (status != 200)
//            return status;
//
//        // parse cookies
//        String cookieString;
//        if ((cookieString = con.getHeaderField("Set-Cookie")) != null)
//            HttpCookie.parse(cookieString).forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));
//
//        // read output
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String line;
//        StringBuffer response = new StringBuffer();
//        while ((line = in.readLine()) != null)
//            response.append(line);
//        in.close();
//        System.out.printf("Response: %s%n", response.toString());
//
//        con.disconnect();
//        return status;
//    }
//
//    private int request(String uri, String method) throws Exception {
//        return request(uri, method, "", new ArrayList<>());
//    }
//
//    private int request(String uri, String method, String data) throws Exception {
//        return request(uri, method, data, new ArrayList<>());
//    }
//
//
//
//    public void testPhotocol() throws Exception {
//        request("/image/cat.jpg", "GET");
////        request("/login", "POST", "{\"email\":\"jlam@cooper.edu\"," +
////                "\"passwordHash\":\"*&^%E(&%^$#*&%*(%($#^%&%*(&(TR*&$*&TU*&$%&^$^#\"}");
//        // create new user
//        request("/signup","POST"," {\"email\":\"whatever@gmail.com\",\"passwordHash\":\"passowordpassword\"}");
//        //try to create user that already exists
//        request("/signup","POST"," {\"email\":\"victorzh716@gmail.com\",\"passwordHash\":\"passowordpassword\"}");
//        //login with nonexisitng user
//        request("/login", "POST", "{\"email\":\"idontexist@gmail.com\",\"passwordHash\":\"password\"}");
//        //login using wrong password
//        request("/login", "POST", "{\"email\":\"victorzh716@gmail.com\",\"passwordHash\":\"notcorrectpassword\"}");
//        //login using exisitng user and right password
//        request("/login", "POST", "{\"email\":\"victorzh716@gmail.com\",\"passwordHash\":\"password\"}");
//        //login in again when logged in already
//        request("/login", "POST", "{\"email\":\"victorzh716@gmail.com\",\"passwordHash\":\"password\"}");
//        //put photo in a collection that is not public
//        request("/collection/new", "POST", "{\"name\":\"some collection\",\"isPublic\":false}");
//        //put photo in a collection that is public
//        request("/collection/new", "POST", "{\"name\":\"some collection\",\"isPublic\":true}");
//        //list all the photos in the collection
//        request("/collection/collection1/photos", "GET");
//
//        request("/collection/collection1/addphoto", "POST", "{\"uri\":\"418120749463199.jpg\"}");
//        request("/collection/collection1/addphoto", "POST", "{\"uri\":\"thisimagedoesntexist.jpg\"}");
//
//        request("/collection/collection1/photos", "GET");
//
//        request("/userphotos", "GET");
//        request("/usercollections", "GET");
//
//        // this will print ugly stuff to terminal
////        request("/image/cat.jpg", "GET");
//
//        // try this with your own image
////        List<String[]> headers = new ArrayList<>();
////        headers.add(new String[]{"Content-Type", "image/jpeg"});
////        request("/image/test.png/upload", "PUT", "/home/jon/Downloads/cat2.jpg", headers);
//
//        request("/logout", "GET");
//        request("/image/cat.jpg", "GET");
//    }
}
