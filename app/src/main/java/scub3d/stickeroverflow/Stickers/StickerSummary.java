package scub3d.stickeroverflow.Stickers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Item;

/**
 * Created by scub3d on 2/27/18.
 */

public class StickerSummary extends Item implements Parcelable, Comparable<StickerSummary> {
    private int numberOwned;
    private ArrayList<HackathonSummary> hackathonsReceivedAt;
    private float percentMatch;

    public StickerSummary() {}

    public StickerSummary(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public StickerSummary(String id, String name, int numberOwned, ArrayList<HackathonSummary> hackathonsReceivedAt) {
        this.id = id;
        this.name = name;
        this.numberOwned = numberOwned;
        this.hackathonsReceivedAt = hackathonsReceivedAt;
    }

    public StickerSummary(String id, String name, int numberOwned, float percentMatch, ArrayList<HackathonSummary> hackathonsReceivedAt) {
        this.id = id;
        this.name = name;
        this.numberOwned = numberOwned;
        this.percentMatch = percentMatch;
        this.hackathonsReceivedAt = hackathonsReceivedAt;
    }

    protected StickerSummary(Parcel in) {
        id = in.readString();
        name = in.readString();
        numberOwned = in.readInt();
    }

    public static final Creator<StickerSummary> CREATOR = new Creator<StickerSummary>() {
        @Override
        public StickerSummary createFromParcel(Parcel in) {
            return new StickerSummary(in);
        }

        @Override
        public StickerSummary[] newArray(int size) {
            return new StickerSummary[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(numberOwned);
    }

    public int getNumberOwned() {
        return numberOwned;
    }

    public void setNumberOwned(int numberOwned) {
        this.numberOwned = numberOwned;
    }

    public ArrayList<HackathonSummary> getHackathonsReceivedAt() {
        return hackathonsReceivedAt;
    }

    public void setHackathonsReceivedAt(ArrayList<HackathonSummary> hackathonsReceivedAt) {
        this.hackathonsReceivedAt = hackathonsReceivedAt;
    }

    public float getPercentMatch() {
        return percentMatch;
    }

    public void setPercentMatch(float percentMatch) {
        this.percentMatch = percentMatch;
    }

    public int compareTo(StickerSummary compareSticker) {
        float comparePercentMatch = ((StickerSummary) compareSticker).getPercentMatch();
        return this.getPercentMatch() > comparePercentMatch ? -1: this.getPercentMatch() < comparePercentMatch ? 1 : 0;
    }

}
