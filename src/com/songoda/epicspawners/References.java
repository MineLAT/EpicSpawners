package com.songoda.epicspawners;

public class References {

    private String prefix = null;

    public References() {
        prefix = Lang.PREFIX.getConfigValue() + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}
