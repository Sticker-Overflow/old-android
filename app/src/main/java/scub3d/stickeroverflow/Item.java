package scub3d.stickeroverflow;

/**
 * Created by scub3d on 2/20/18.
 */

public class Item {
    protected String id, name;

    public Item() {
    }

    public Item(String id, String name) {
        this.name = name;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
