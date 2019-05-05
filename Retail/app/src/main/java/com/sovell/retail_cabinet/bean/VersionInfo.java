package com.sovell.retail_cabinet.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

//<?xml version="1.0"encoding="UTF-8"?>
//<Update id="getUpdateInfo">
//        <versionCode>101</versionCode>
//        <versionName>V1.0.1</versionName>
//        <apkName>picktable_V1.0.1.apk</apkName>
//</Update>

@Root(name = "Update", strict = false)
public class VersionInfo {

    @Element(name = "versionCode", required = false)
    public int versionCode;

    @Element(name = "versionName", required = false)
    public String versionName;

    @Element(name = "apkName", required = false)
    public String apkName;

    @Element(name = "hintMsg", required = false)
    public String hintMsg;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getHintMsg() {
        return hintMsg;
    }

    public void setHintMsg(String hintMsg) {
        this.hintMsg = hintMsg;
    }
}
