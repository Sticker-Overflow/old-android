package scub3d.stickeroverflow.Hackathons;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

import scub3d.stickeroverflow.Item;

/**
 * Created by scub3d on 2/7/18.
 */


public class HackathonSummary extends Item implements Parcelable {
    protected String location, dateString, logoURL, splashURL;
    protected Date date;

    public HackathonSummary() {
    }

    public HackathonSummary(String id, String name, String dateString, Date date, String location, String url, String logoURL, String splashURL) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.dateString = dateString;
        this.date = date;
        this.logoURL = logoURL;
        this.splashURL = splashURL;
    }

    protected HackathonSummary(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        dateString = in.readString();
        logoURL = in.readString();
        splashURL = in.readString();
    }

    public static final Creator<HackathonSummary> CREATOR = new Creator<HackathonSummary>() {
        @Override
        public HackathonSummary createFromParcel(Parcel in) {
            return new HackathonSummary(in);
        }

        @Override
        public HackathonSummary[] newArray(int size) {
            return new HackathonSummary[size];
        }
    };

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String date) {
        this.dateString = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSplashURL() {
        return splashURL;
    }

    public void setSplashURL(String splashURL) {
        this.splashURL = splashURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(dateString);
        dest.writeString(logoURL);
        dest.writeString(splashURL);
    }
}
