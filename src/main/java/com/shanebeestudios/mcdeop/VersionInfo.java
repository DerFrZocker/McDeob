package com.shanebeestudios.mcdeop;

public interface VersionInfo {

    String getVersion();

    String getJar();

    String getMappings();

    Version.Type getType();

    boolean isLatest();
}
