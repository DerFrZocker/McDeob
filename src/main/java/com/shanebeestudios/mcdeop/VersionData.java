package com.shanebeestudios.mcdeop;

public record VersionData(Version.Type type, String version, String jar, String mappings) implements VersionInfo {
    @Override
    public String getVersion() {
        return version();
    }

    @Override
    public String getJar() {
        return Version.OBJECTS + jar();
    }

    @Override
    public String getMappings() {
        return Version.OBJECTS + mappings();
    }

    @Override
    public Version.Type getType() {
        return type();
    }

    @Override
    public boolean isLatest() {
        return false;
    }
}
