package scub3d.stickeroverflow.Organizers;

import android.os.Parcel;
import android.os.Parcelable;

import scub3d.stickeroverflow.Item;

/**
 * Created by scub3d on 2/7/18.
 */

public class OrganizerSummary extends Item implements Parcelable {

    protected String location, logoURL;

    public OrganizerSummary() {
    }

    public OrganizerSummary(String id, String name, String location, String logoURL) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.logoURL = logoURL;
    }

    protected OrganizerSummary(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        logoURL = in.readString();
    }

    public static final Creator<OrganizerSummary> CREATOR = new Creator<OrganizerSummary>() {
        @Override
        public OrganizerSummary createFromParcel(Parcel in) {
            return new OrganizerSummary(in);
        }

        @Override
        public OrganizerSummary[] newArray(int size) {
            return new OrganizerSummary[size];
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

