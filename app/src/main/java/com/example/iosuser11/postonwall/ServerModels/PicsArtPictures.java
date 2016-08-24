package com.example.iosuser11.postonwall.ServerModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by iosuser9 on 18.08.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class PicsArtPictures {
    public long id;
    public String url;

    public PicsArtPictures() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
