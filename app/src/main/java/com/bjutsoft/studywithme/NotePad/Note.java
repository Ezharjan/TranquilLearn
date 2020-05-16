package com.bjutsoft.studywithme.NotePad;

import org.litepal.crud.LitePalSupport;

public class Note extends LitePalSupport {

    private String mcontent;
    private String mtime;

    public String getContent() {
        return mcontent;
    }

    public void setContent(String mcontent) {
        this.mcontent = mcontent;
    }

    public String getTime() {
        return mtime;
    }

    public void setTime(String mtime) {
        this.mtime = mtime;
    }
}
