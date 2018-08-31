package scub3d.stickeroverflow.Stickers;

import java.util.ArrayList;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Organizers.OrganizerSummary;

/**
 * Created by scub3d on 2/10/18.
 */

public class Sticker extends StickerSummary {
    private String description, purchaseUrl;
    private int numberOfUsersWhoHaveThisSticker;
    private ArrayList<HackathonSummary> hackathons;
    private ArrayList<OrganizerSummary> organizations;

    public Sticker() { }

    public Sticker(String id, String name, String description, String purchaseUrl, int numberOfUsersWhoHaveThisSticker, ArrayList<HackathonSummary> hackathons, ArrayList<OrganizerSummary> organizations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.purchaseUrl = purchaseUrl;
        this.numberOfUsersWhoHaveThisSticker = numberOfUsersWhoHaveThisSticker;
        this.hackathons = hackathons;
        this.organizations = organizations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<HackathonSummary> getHackathons() {
        return hackathons;
    }

    public void setHackathons(ArrayList<HackathonSummary> hackathons) {
        this.hackathons = hackathons;
    }

    public ArrayList<OrganizerSummary> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(ArrayList<OrganizerSummary> organizations) {
        this.organizations = organizations;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public int getNumberOfUsersWhoHaveThisSticker() {
        return numberOfUsersWhoHaveThisSticker;
    }

    public void setNumberOfUsersWhoHaveThisSticker(int numberOfUsersWhoHaveThisSticker) {
        this.numberOfUsersWhoHaveThisSticker = numberOfUsersWhoHaveThisSticker;
    }
}
