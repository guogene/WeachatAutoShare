package com.gene.guo.quanautoshare.datainitialize;

public class MomentItemBean {
    private String tilte;
    private String[] urls;

    public void setTilte(String tilte){
        this.tilte = tilte;
    }

    public void setUrls(String[] urls){
        this.urls = urls;
    }

    public java.lang.String getTilte() {
        return tilte;
    }

    public String[] getUrls() {
        return urls;
    }
}