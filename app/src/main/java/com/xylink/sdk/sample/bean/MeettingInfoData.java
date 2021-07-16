package com.xylink.sdk.sample.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity(nameInDb = "meeting_data")
public class MeettingInfoData implements Parcelable {
    @Property(nameInDb = "Id")
    @Id(autoincrement = true)
    private Long id;
    private String meettingName;
    private String meettingNum;
    private String reMark;
    private long time;
    @Generated(hash = 1520982710)
    public MeettingInfoData(Long id, String meettingName, String meettingNum,
                            String reMark, long time) {
        this.id = id;
        this.meettingName = meettingName;
        this.meettingNum = meettingNum;
        this.reMark = reMark;
        this.time = time;
    }
    @Generated(hash = 30450046)
    public MeettingInfoData() {
    }

    protected MeettingInfoData(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        meettingName = in.readString();
        meettingNum = in.readString();
        reMark = in.readString();
        time = in.readLong();
    }

    public static final Creator<MeettingInfoData> CREATOR = new Creator<MeettingInfoData>() {
        @Override
        public MeettingInfoData createFromParcel(Parcel in) {
            return new MeettingInfoData(in);
        }

        @Override
        public MeettingInfoData[] newArray(int size) {
            return new MeettingInfoData[size];
        }
    };

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMeettingName() {
        return this.meettingName;
    }
    public void setMeettingName(String meettingName) {
        this.meettingName = meettingName;
    }
    public String getMeettingNum() {
        return this.meettingNum;
    }
    public void setMeettingNum(String meettingNum) {
        this.meettingNum = meettingNum;
    }
    public String getReMark() {
        return this.reMark;
    }
    public void setReMark(String reMark) {
        this.reMark = reMark;
    }
    public long getTime() {
        return this.time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(meettingName);
        parcel.writeString(meettingNum);
        parcel.writeString(reMark);
        parcel.writeLong(time);
    }
}

