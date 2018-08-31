package scub3d.stickeroverflow.Stickers;

import java.util.ArrayList;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;

/**
 * Created by scub3d on 3/7/18.
 */

public class StickerForUser extends StickerSummary {
    private int numberOwned;
    private ArrayList<HackathonSummary> hackathonsReceivedAt;

    public StickerForUser() { }

    public StickerForUser(String id, String name, int numberOwned, ArrayList<HackathonSummary> hackathonsReceivedAt) {
        this.id = id;
        this.name = name;
        this.numberOwned = numberOwned;
        this.hackathonsReceivedAt = hackathonsReceivedAt;
    }

    public String getHackathonReceivedAt() {
        if(hackathonsReceivedAt.size() == 0) {
            return "Unknown";
        } else if(hackathonsReceivedAt.size() > 1) {
            return "Various Hackathons";
        }
        return hackathonsReceivedAt.get(0).getName();
    }

    public void setHackathonsReceivedAt(ArrayList<HackathonSummary> hackathonsReceivedAt) {
        this.hackathonsReceivedAt = hackathonsReceivedAt;
    }

    public int getNumberOwned() {
        return numberOwned;
    }

    public void setNumberOwned(int numberOwned) {
        this.numberOwned = numberOwned;
    }

}
