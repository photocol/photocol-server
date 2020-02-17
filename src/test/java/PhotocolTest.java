import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import photocol.Photocol;
import spark.Spark;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;

public class PhotocolTest extends TestCase {

    public PhotocolTest() {
        // start webserver and wait for it to load
        Photocol.main(new String[]{});
        Spark.awaitInitialization();

        // set up cookie manager for managing sessions
        cookieManager = new CookieManager();
    }

    // for sending test requests to the webserver; helpful guide to managing HTTP requests from Java: (w/ cookies):
    // https://www.baeldung.com/java-http-request
    private CookieManager cookieManager;
    private void request(String uri, String method) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:" + Spark.port() + uri)
                .openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Cookie", StringUtils.join(cookieManager.getCookieStore().getCookies(), ";"));

        // send request
        String cookieString;
        if((cookieString = con.getHeaderField("Set-Cookie")) != null)
            HttpCookie.parse(cookieString) .forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));
    }

    public void testPhotocol() throws Exception {
        // can write assertion tests here
        request("/login", "GET");
        request("/signup", "GET");
        request("/login", "GET");
        request("/logout", "GET");
        request("/login", "GET");
    }
}
