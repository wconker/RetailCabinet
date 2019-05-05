package com.sovell.retail_cabinet.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 下载时的对象
 */

public class BaseDownLoad implements Parcelable {

    private int progress;
    private long currentFileSize;
    private long totalFileSize;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCurrentFileSize() {
        return currentFileSize;
    }

    public void setCurrentFileSize(long currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.progress);
        dest.writeLong(this.currentFileSize);
        dest.writeLong(this.totalFileSize);
    }

    public BaseDownLoad() {
    }

    protected BaseDownLoad(Parcel in) {
        this.progress = in.readInt();
        this.currentFileSize = in.readLong();
        this.totalFileSize = in.readLong();
    }

    public static final Creator<BaseDownLoad> CREATOR = new Creator<BaseDownLoad>() {
        @Override
        public BaseDownLoad createFromParcel(Parcel source) {
            return new BaseDownLoad(source);
        }

        @Override
        public BaseDownLoad[] newArray(int size) {
            return new BaseDownLoad[size];
        }
    };
}
