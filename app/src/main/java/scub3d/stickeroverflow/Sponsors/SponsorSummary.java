package scub3d.stickeroverflow.Sponsors;

import android.os.Parcel;
import android.os.Parcelable;

import scub3d.stickeroverflow.Item;

/**
 * Created by scub3d on 2/7/18.
 */

public class SponsorSummary extends Item implements Parcelable {

    protected String location, logoURL;

    public SponsorSummary() {
    }

    public SponsorSummary(String id, String name, String location, String logoURL) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.logoURL = logoURL;
    }

    protected SponsorSummary(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        logoURL = in.readString();
    }

    public static final Creator<SponsorSummary> CREATOR = new Creator<SponsorSummary>() {
        @Override
        public SponsorSummary createFromParcel(Parcel in) {
            return new SponsorSummary(in);
        }

        @Override
        public SponsorSummary[] newArray(int size) {
            return new SponsorSummary[size];
        }
    };

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String location) {
        this.logoURL = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
        dest.writeString(logoURL);
    }
}

