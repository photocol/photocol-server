import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import photocol.Photocol;
import spark.Spark;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;

public class PhotocolTest extends TestCase {

    public PhotocolTest() {
        // start webserver and wait for it to load
        Photocol.main(new String[]{});
        Spark.awaitInitialization();
    }

    // for sending test requests to the webserver; helpful guide to managing HTTP requests from Java: (w/ cookies):
    // https://www.baeldung.com/java-http-request; this imitates a browser session by maintaining cookies
    private CookieManager cookieManager = new CookieManager();
    private int request(String uri, String method, String data) throws Exception {
        String url = "http://localhost:" + Spark.port() + uri;
        System.out.printf("Testing %s request to %s with data \"%s\".%n", url, method, data);
        HttpURLConnection con = (HttpURLConnection) new URL(url)
                .openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Cookie", StringUtils.join(cookieManager.getCookieStore().getCookies(), ";"));
        if(method.equals("POST")) {
            con.setRequestProperty("Content-Type", "application/json");
            if(data.length()>0) {
                con.setDoOutput(true);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
                out.write(data);
                out.close();
            }
        }

        // send request and get status
        int status = con.getResponseCode();
        if(status != 200)
            return status;

        // parse cookies
        String cookieString;
        if((cookieString = con.getHeaderField("Set-Cookie")) != null)
            HttpCookie.parse(cookieString).forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));

        // read output
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();
        while((line = in.readLine()) != null)
            response.append(line);
        in.close();
        System.out.printf("Response: %s%n", response.toString());

        con.disconnect();
        return status;
    }
    private int request(String uri, String method) throws Exception {
        return request(uri, method, "");
    }

    public void testPhotocol() throws Exception {
            request("/image/4k.jpg", "GET" );
//        // can run assertions here (e.g., check status code of requests)

//        assertEquals(200, request("/login", "POST",
//                "{\"username\":\"man\",\"passwordHash\":\"abcdef\"}"));
//        assertEquals(200, request("/signup", "POST",
//                "{\"username\":\"test\",\"passwordHash\":\"abcdef\"}"));
//        assertEquals(200,request("/userdetails", "GET"));
//        assertEquals(200, request("/login", "POST",
//                "{\"username\":\"test\",\"passwordHash\":\"abcdef\"}"));
//        assertEquals(200, request("/login", "POST",
//                "{\"username\":\"test\",\"passwordHash\":\"abc\"}"));
//        assertEquals(200, request("/logout", "GET"));
//        assertEquals(200, request("/login", "POST",
//                "{\"username\":\"test\",\"passwordHash\":\"abc\"}"));
//        assertEquals(200,request("/userdetails", "GET"));
//        assertEquals(404, request("/abc", "GET"));
    }
}
