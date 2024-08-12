/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;

import com.google.gson.JsonObject;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.spi.utils.GalasaGson;

public class UserRouteTest extends BaseServletTest {

    private String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0UmVxdWVzdG9yIiwic3ViIjoicmVxdWVzdG9ySWQiLCJuYW1lIjoiSmFjayBTa2VsbGluZ3RvbiIsImlhdCI6MTUxNjIzOTAyMn0.kW1arFknbywrtRrxsLjB2MiXcM6oSgnUrOpuAlE5dhk"; //Dummy JWT                                                                                                                                                                                                                                            // secret
    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + jwt);

    private static final GalasaGson gson = new GalasaGson();

    @Test
    public void testUsersGetRequestWithMissingNameParamReturnsBadRequest() throws Exception {
        // Given...
        UsersServlet servlet = new UsersServlet();

        Map<String, String[]> queryParams = new HashMap<>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/users/name'. Please check your request parameters or report the problem to
        // your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testUsersGetRequestReturnsArrayOfUsersReturns_OK() throws Exception {
        // Given...
        UsersServlet servlet = new UsersServlet();

        String requestorLoginId = "me";

        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("name", new String[] { requestorLoginId });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/users", headerMap);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting an array of json objects:
        // [{
        // "login_id": "testRequestor",
        // }]

        String gotBackPayload = outStream.toString();
        String expectedPayload = 
            "[\n"+
            "  {\n"+
            "    \"login_id\": \"testRequestor\"\n"+
            "  }\n"+
            "]";

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

}
