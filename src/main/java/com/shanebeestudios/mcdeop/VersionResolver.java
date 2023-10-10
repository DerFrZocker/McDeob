package com.shanebeestudios.mcdeop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VersionResolver {
    private final File VERSION_DATA = new File("versions");
    private final File VERSION_MANIFEST = new File(VERSION_DATA, "version_manifest.json");
    private final URI VERSION_MANIFEST_URI = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json");

    public VersionData resolve(Version.Type type, String version) throws IOException, InterruptedException {
        File versionFile = new File(VERSION_DATA, version + ".json");


        if (!versionFile.exists()) {
            loadVersionData(version);
        }

        try (Reader reader = new FileReader(new File(VERSION_DATA, version + ".json"))) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject downloads = root.getAsJsonObject("downloads");
            String jar = downloads.getAsJsonObject(type.getName().toLowerCase(Locale.ROOT)).get("sha1").getAsString() + "/"+ type.getName() + ".jar";
            String mapping = downloads.getAsJsonObject(type.getName().toLowerCase(Locale.ROOT) + "_mappings").get("sha1").getAsString() + "/"+ type.getName() + ".txt";

            return new VersionData(type, version, jar, mapping);
        }
    }

    public void createVersionFile() throws IOException {
        try (Writer writer = new FileWriter("versions.txt"); Reader reader = new FileReader(VERSION_MANIFEST)){
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            LinkedList<String> versions = new LinkedList<>();
            for (JsonElement element : root.getAsJsonArray("versions")) {
               versions.add(element.getAsJsonObject().get("id").getAsString());
            }

            for (Iterator<String> it = versions.descendingIterator(); it.hasNext(); ) {
                String version = it.next();

                writer.write(version);
                writer.write(System.lineSeparator());
            }
        }
    }

    private void loadVersionData(String version) throws IOException, InterruptedException {
        if (!VERSION_MANIFEST.exists()) {
            loadVersionManifest();
        } else if (VERSION_MANIFEST.lastModified() < (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2))) {
            VERSION_MANIFEST.delete();
            loadVersionManifest();
        }

        try (Reader reader = new FileReader(VERSION_MANIFEST)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (JsonElement element : root.getAsJsonArray("versions")) {
                if (element.getAsJsonObject().get("id").getAsString().equals(version)) {

                    HttpClient httpClient = HttpClient.newHttpClient();
                    httpClient.send(HttpRequest.newBuilder(URI.create(element.getAsJsonObject().get("url").getAsString())).GET().build(), HttpResponse.BodyHandlers.ofFile(new File(VERSION_DATA, version + ".json").toPath()));
                    return;
                }
            }
        }

        throw new IllegalArgumentException("No version found for " + version);
    }

    private void loadVersionManifest() throws IOException, InterruptedException {
        VERSION_DATA.mkdirs();

        HttpClient httpClient = HttpClient.newHttpClient();
        httpClient.send(HttpRequest.newBuilder(VERSION_MANIFEST_URI).GET().build(), HttpResponse.BodyHandlers.ofFile(VERSION_MANIFEST.toPath()));
    }
}
