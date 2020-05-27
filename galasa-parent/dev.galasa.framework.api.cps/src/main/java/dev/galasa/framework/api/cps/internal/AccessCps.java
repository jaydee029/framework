/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.cps.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * CPS API
 * 
 * Allows for CPS properties to be retrieved and added
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/cps/*" }, name = "Galasa CPS")
public class AccessCps extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            Pattern pattern1 = Pattern.compile("/namespace/?");
            Matcher matcher1 = pattern1.matcher(req.getPathInfo());
            Pattern pattern2 = Pattern.compile("/namespace/([A-z0-9]+)/?");
            Matcher matcher2 = pattern2.matcher(req.getPathInfo());
            Pattern pattern3 = Pattern.compile("/namespace/([A-z0-9]+)/prefix/([A-z0-9._\\-]+)/suffix/([A-z0-9._\\-]+)/?");
            Matcher matcher3 = pattern3.matcher(req.getPathInfo());
            if(matcher1.matches()) {
                getNamespaces(resp);
            } else if(matcher2.matches()) {
                getNamespaceProperties(resp, matcher2.group(1));
            } else if(matcher3.matches()) {
                getCPSProperty(resp, matcher3.group(1), matcher3.group(2), matcher3.group(3), req.getQueryString());
            } else {
                sendError(resp, "Invalid GET URL - " + req.getPathInfo());
            }
        } catch (IOException e) {
            sendError(resp, e.getStackTrace());
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            Pattern pattern1 = Pattern.compile("/namespace/([A-z0-9]+)/property/([A-z0-9._\\-]+)/?");
            Matcher matcher1 = pattern1.matcher(req.getPathInfo());
            if(matcher1.matches()) {
                String namespace = matcher1.group(1);
                String property = matcher1.group(2);
                JsonObject reqJson = gson.fromJson(new InputStreamReader(req.getInputStream()),JsonObject.class);
                if(!property.equals(reqJson.get("name").getAsString())) {
                    sendError(resp, "Different CPS property name in url and request: " + property + ", " + reqJson.get("name"));
                } else {
                    IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
                    cps.setProperty(reqJson.get("name").getAsString(), reqJson.get("value").getAsString());
                    resp.setStatus(200);
                }
            } else {
                sendError(resp, "Invalid PUT URL - " + req.getPathInfo());
            }
        } catch (IOException | ConfigurationPropertyStoreException e) {
            sendError(resp, e.getStackTrace());
        }
    }

    private void getNamespaces(HttpServletResponse resp) throws IOException {
        JsonArray namespaceArray = new JsonArray();
            try {
                List<String> namespaces = framework.getConfigurationPropertyService("framework").getCPSNamespaces();
                for(String name : namespaces) {
                    namespaceArray.add(name);
                }
            } catch(ConfigurationPropertyStoreException e) {
                logger.error("Unable to access CPS", e);
                resp.setStatus(500);
                return;
            }
            resp.getWriter().write(gson.toJson(namespaceArray));
            resp.setStatus(200);
            return;
    }

    private void getNamespaceProperties(HttpServletResponse resp, String namespace) throws IOException {
        JsonArray propertyArray = new JsonArray();
        try {
            Map<String,String> properties = framework.getConfigurationPropertyService(namespace).getAllProperties();
            for(String prop : properties.keySet()) {
                JsonObject cpsProp = new JsonObject();
                cpsProp.addProperty("name", prop);
                cpsProp.addProperty("value", properties.get(prop));
                propertyArray.add(cpsProp);
            }
        } catch(ConfigurationPropertyStoreException e) {
            logger.error("Unable to access CPS", e);
            resp.setStatus(500);
            return;
        }
        resp.getWriter().write(gson.toJson(propertyArray));
        resp.setStatus(200);
        return;
    }

    private void getCPSProperty(HttpServletResponse resp, String namespace, String prefix, String suffix, String infixQuery)
            throws IOException {
        String[] infixArray = null;
        if(infixQuery == null) {
            infixArray = new String[0];
        } else {
            String[] queries = infixQuery.split("&");
            List<String> infixes = new ArrayList<>();
            for(String pair : queries) {
                String[] keyValue = pair.split("=");
                if(!keyValue[0].equals("infix")) {
                    logger.error("Invalid Infix in URL");
                    resp.setStatus(500);
                    return;
                }
                infixes.add(keyValue[1]);
            }
            infixArray = infixes.toArray(new String[0]);
        }
        JsonObject respJson = new JsonObject();

        try {
            String propValue = framework.getConfigurationPropertyService(namespace).getProperty(prefix, suffix, infixArray);
            Map<String, String> pairs = framework.getConfigurationPropertyService(namespace).getAllProperties();
            for(String key : pairs.keySet()) {
                if(pairs.get(key).equals(propValue) && key.startsWith(namespace + "." + prefix) && key.endsWith(suffix)) {
                    respJson.addProperty("name", key);
                    respJson.addProperty("value", pairs.get(key));
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Unable to Access CPS");
            resp.setStatus(500);
            return;
        }
        resp.getWriter().write(gson.toJson(respJson));
        resp.setStatus(200);
        return;
    }

    public void sendError(HttpServletResponse resp, StackTraceElement[] trace) {
        StringBuilder message = new StringBuilder();
        for(StackTraceElement element : trace) {
            message.append(element.toString());
        }
        sendError(resp, message.toString());
    }

    public void sendError(HttpServletResponse resp, String trace) {
        resp.setStatus(500);
        JsonObject json = new JsonObject();
        json.addProperty("error", trace);
        try {
            resp.getWriter().write(gson.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa CPS API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        // TODO set the JWT signing key etc
    }

    @Deactivate
    void deactivate() {
        // TODO Clear the properties to prevent JWT generation
    }

}
