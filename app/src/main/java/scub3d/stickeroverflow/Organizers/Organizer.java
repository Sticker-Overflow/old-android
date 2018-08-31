package scub3d.stickeroverflow.Organizers;

import android.os.Parcelable;

import java.util.ArrayList;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Users.User;

/**
 * Created by scub3d on 2/7/18.
 */

public class Organizer extends OrganizerSummary implements Parcelable {

    private String url, description;
    private ArrayList<StickerSummary> stickers;
    private ArrayList<HackathonSummary> hackathons;
    private ArrayList<User> members, admins;

    public Organizer() {
    }

    public Organizer(String id, String name, String description, String url, String location, String logoURL, ArrayList<StickerSummary> stickers, ArrayList<HackathonSummary> hackathons, ArrayList<User> members, ArrayList<User> admins) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.url = url;
        this.logoURL = logoURL;
        this.stickers = stickers;
        this.hackathons = hackathons;
        this.members = members;
        this.admins = admins;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<StickerSummary> getStickers() {
        return stickers;
    }

    public void setStickers(ArrayList<StickerSummary> stickers) {
        this.stickers = stickers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<User> getAdmins() {
        return admins;
    }

    public void setAdmins(ArrayList<User> admins) {
        this.admins = admins;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }

    public ArrayList<HackathonSummary> getHackathons() {
        return hackathons;
    }

    public void setHackathons(ArrayList<HackathonSummary> hackathons) {
        this.hackathons = hackathons;
    }
}
