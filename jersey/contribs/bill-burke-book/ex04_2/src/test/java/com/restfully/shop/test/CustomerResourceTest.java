package com.restfully.shop.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class CustomerResourceTest {
    @Test
    public void testCustomerResource() throws Exception {
        System.out.println("*** Create a new Customer ***");
        // Create a new customer
        String newCustomer = "<customer>"
                + "<first-name>Bill</first-name>"
                + "<last-name>Burke</last-name>"
                + "<street>256 Clarendon Street</street>"
                + "<city>Boston</city>"
                + "<state>MA</state>"
                + "<zip>02115</zip>"
                + "<country>USA</country>"
                + "</customer>";

        URL postUrl = new URL("http://localhost:9095/customers");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStream os = connection.getOutputStream();
        os.write(newCustomer.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        System.out.println("Location: " + connection.getHeaderField("Location"));
        connection.disconnect();


        // Get the new customer
        System.out.println("*** GET Created Customer **");
        URL getUrl = new URL("http://localhost:9095/customers/1");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("Content-Type: " + connection.getContentType());

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();


        connection.disconnect();

        // use first last
        System.out.println("**** Use first-name ***");
        getUrl = new URL("http://localhost:9095/customers/Bill-Burke");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");

        System.out.println("Content-Type: " + connection.getContentType());
        reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers");
        WebResource wr1; // http://localhost:9095/customers/2
        WebResource wr2; // http://localhost:9095/customers/Pavel-Bucek

        System.out.println("*** Create a new Customer ***");
        // Create a new customer
        String newCustomer = "<customer>"
                + "<first-name>Pavel</first-name>"
                + "<last-name>Bucek</last-name>"
                + "<street>Top Secret 123</street>"
                + "<city>Prague</city>"
                + "<state>N/A</state>"
                + "<zip>12000</zip>"
                + "<country>Czech Republic</country>"
                + "</customer>";

        ClientResponse response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, newCustomer);

        Assert.assertEquals(201, response.getStatus()); // 201 = created
        System.out.println("Location: " + response.getHeaders().get("Location"));

        // Get the new customer
        System.out.println("*** GET Created Customer **");
        wr1 = wr.path("2"); // second customer
        response = wr1.get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok


        // use first last
        System.out.println("**** Use first-name ***");
        wr2 = wr.path("Pavel-Bucek");
        response = wr2.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));
        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }
}
