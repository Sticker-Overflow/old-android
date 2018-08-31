package scub3d.stickeroverflow;

import org.json.JSONException;

/**
 * Created by scub3d on 11/01/18.
 */

public interface HandleResponseInterface {
    void handleResponse(String response) throws JSONException;
    void handleError();
}
