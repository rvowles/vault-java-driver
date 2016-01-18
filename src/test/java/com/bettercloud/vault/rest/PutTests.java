package com.bettercloud.vault.rest;

import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests relating the REST client processing of PUT requests.
 */
public class PutTests {

    /**
     * Verify a basic PUT request, with no parameters or headers.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPut_Plain() throws RestException, UnsupportedEncodingException {
        final Response response = new Rest()
                .url("https://httpbin.org/put")
                .put();
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getMimeType());

        final String jsonString = new String(response.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/put", jsonObject.getString("url", null));
    }

    /**
     * Verify a PUT request that has no query string on the base URL, but does have additional
     * parameters passed.  The base URL should remain unmodified, and the parameters should be
     * sent with the request body.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPut_InsertParams() throws RestException, UnsupportedEncodingException {
        final Response response = new Rest()
                .url("https://httpbin.org/put")
                .parameter("foo", "bar")
                .parameter("apples", "oranges")
                .parameter("multi part", "this parameter has whitespace in its name and value")
                .put();
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getMimeType());

        final String jsonString = new String(response.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/put", jsonObject.getString("url", null));

        // Note that with a PUT (as with a POST) to this "httpbin.org" test service, parameters are
        // returned within a JSON object called "form", unlike it's "args" counterpart when doing a GET.
        final JsonObject form = jsonObject.get("form").asObject();
        assertEquals("bar", form.getString("foo", null));
        assertEquals("oranges", form.getString("apples", null));
        assertEquals("this parameter has whitespace in its name and value", form.getString("multi part", null));
    }

    /**
     * Verify a PUT request that already has a query string on the base URL, but also has additional
     * parameters passed.  The base URL should remain unmodified, and the parameters should be
     * sent with the request body.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPut_UpdateParams() throws RestException, UnsupportedEncodingException {
        final Response response = new Rest()
                .url("https://httpbin.org/put?hot=cold")
                .parameter("foo", "bar")
                .parameter("apples", "oranges")
                .parameter("multi part", "this parameter has whitespace in its name and value")
                .put();
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getMimeType());

        final String jsonString = new String(response.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/put?hot=cold", jsonObject.getString("url", null));
        final JsonObject args = jsonObject.get("args").asObject();
        assertEquals("cold", args.getString("hot", null));
        final JsonObject form = jsonObject.get("form").asObject();
        assertEquals("bar", form.getString("foo", null));
        assertEquals("oranges", form.getString("apples", null));
        assertEquals("this parameter has whitespace in its name and value", form.getString("multi part", null));
    }

    /**
     * <p>Verify a PUT request that passes HTTP headers.</p>
     *
     * <p>Note that even though our header names are all lowercase, the round-trip process
     * converts them to camel case (e.g. <code>two-part</code> to <code>Two-Part</code>).</p>
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPut_WithHeaders() throws RestException, UnsupportedEncodingException {
        final Response response = new Rest()
                .url("https://httpbin.org/put")
                .header("black", "white")
                .header("day", "night")
                .header("two-part", "Note that headers are send in url-encoded format")
                .put();
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getMimeType());

        final String jsonString = new String(response.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/put", jsonObject.getString("url", null));
        final JsonObject headers = jsonObject.get("headers").asObject();
        assertEquals("white", headers.getString("Black", null));
        assertEquals("night", headers.getString("Day", null));
        assertEquals("Note+that+headers+are+send+in+url-encoded+format", headers.getString("Two-Part", null));
    }

}
