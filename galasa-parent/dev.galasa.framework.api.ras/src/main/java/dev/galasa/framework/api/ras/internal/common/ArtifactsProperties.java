/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.routes.RunsRoute;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class ArtifactsProperties implements IRunRootArtifact {

    private RunsRoute runsRoute;

    public ArtifactsProperties(RunsRoute runsRoute) {
        this.runsRoute = runsRoute;
    }

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactsJson = runsRoute.getArtifacts(run);

        // Convert JSON array into a map of key-value pairs, where keys are paths and values are artifact content types,
        // to match the generated artifacts.properties files.
        Map<String, String> artifactsProperties = new HashMap<>();
        for (JsonElement element : artifactsJson) {
            JsonObject artifactObject = element.getAsJsonObject();
            
            artifactsProperties.put(artifactObject.get("path").getAsString(), artifactObject.get("contentType").getAsString());
        }
        
        // Convert the map to a string, where keys and values are separated by =, and
        // each entry is written on a separate line.
        String artifactsPropertiesStr = artifactsProperties.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("\n"));
        
        return artifactsPropertiesStr.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public String getPathName() {
        return "/artifacts.properties";
    }
}
