package scub3d.stickeroverflow.Hackathons;

import java.util.ArrayList;
import java.util.Date;

import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.Stickers.StickerSummary;

/**
 * Created by scub3d on 2/6/18.
 */

public class Hackathon extends HackathonSummary {
    private String url;
    private ArrayList<StickerSummary> stickers;
    private ArrayList<OrganizerSummary> sponsors, organizers;

    public Hackathon() {
    }

    public Hackathon(String id, String name, String dateString, Date date, String location, String url, String logoURL, String splashURL, ArrayList<StickerSummary> stickers, ArrayList<OrganizerSummary> sponsors, ArrayList<OrganizerSummary> organizers) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.dateString = dateString;
        this.date = date;
        this.url = url;
        this.logoURL = logoURL;
        this.splashURL = splashURL;
        this.stickers = stickers;
        this.sponsors = sponsors;
        this.organizers = organizers;
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

    public ArrayList<OrganizerSummary> getSponsors() {
        return sponsors;
    }

    public void setSponsors(ArrayList<OrganizerSummary> sponsors) {
        this.sponsors = sponsors;
    }

    public ArrayList<OrganizerSummary> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(ArrayList<OrganizerSummary> organizers) {
        this.organizers = organizers;
    }
}