package scub3d.stickeroverflow.Users;

import java.util.ArrayList;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Stickers.StickerSummary;

/**
 * Created by scub3d on 3/1/18.
 */

public class User {
    private String uid, name, photoUrl, memberOrganizationId;
    private ArrayList<StickerSummary> stickers;
    private ArrayList<HackathonSummary> upcomingHackathons, attendedHackathons;
    private boolean isAdmin, isSuperUser;

    public User() {
    }

    public User(String uid, boolean isAdmin, boolean isSuperUser, String name, String photoUrl, String memberOrganizationId, ArrayList<HackathonSummary> upcomingHackathons, ArrayList<HackathonSummary> attendedHackathons, ArrayList<StickerSummary> stickers) {
        this.uid = uid;
        this.name = name;
        this.isAdmin = isAdmin;
        this.isSuperUser = isSuperUser;
        this.photoUrl = photoUrl;
        this.memberOrganizationId = memberOrganizationId;
        this.upcomingHackathons = upcomingHackathons;
        this.attendedHackathons = attendedHackathons;
        this.stickers = stickers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String id) {
        this.uid = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<StickerSummary> getStickers() {
        return stickers;
    }

    public void setStickers(ArrayList<StickerSummary> stickers) {
        this.stickers = stickers;
    }

    public String getMemberOrganizationId() {
        return memberOrganizationId;
    }

    public void setMemberOrganizationId(String memberOrganizationId) {
        this.memberOrganizationId = memberOrganizationId;
    }

    public ArrayList<HackathonSummary> getUpcomingHackathons() {
        return upcomingHackathons;
    }

    public void setUpcomingHackathons(ArrayList<HackathonSummary> upcomingHackathons) {
        this.upcomingHackathons = upcomingHackathons;
    }

    public ArrayList<HackathonSummary> getAttendedHackathons() {
        return attendedHackathons;
    }

    public void setAttendedHackathons(ArrayList<HackathonSummary> attendedHackathons) {
        this.attendedHackathons = attendedHackathons;
    }

    public boolean isSuperUser() {
        return isSuperUser;
    }

    public void setSuperUser(boolean superUser) {
        isSuperUser = superUser;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
