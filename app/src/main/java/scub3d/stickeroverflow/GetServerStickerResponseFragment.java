package scub3d.stickeroverflow;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 11/01/18.
 */

public class GetServerStickerResponseFragment extends BaseFragment implements HandleResponseInterface {
    private static final String ARG_PICTURE_PATH = "picturePath";
    private String picturePath;
    private OnFragmentInteractionListener mListener;

    public GetServerStickerResponseFragment() {
        // Required empty public constructor
    }
    
    public static GetServerStickerResponseFragment newInstance(String param1, String param2) {
        GetServerStickerResponseFragment fragment = new GetServerStickerResponseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PICTURE_PATH, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            picturePath = getArguments().getString(ARG_PICTURE_PATH);
        }
    }

    ProgressBar pb;
    TextView pbText;
    RecyclerView recyclerView;

    private boolean imageSent = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.get_server_sticker_response, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (getArguments() != null) {
            picturePath = getArguments().getString(ARG_PICTURE_PATH);
        }

        pb = rootView.findViewById(R.id.pb);
        pbText = rootView.findViewById(R.id.pbText);
        recyclerView = rootView.findViewById(R.id.resultsRecyclerView);

        pbText.setText("Preparing to upload file");
        // Slight delay maybe?
        Log.d(TAG, "onCreate: " + picturePath);
        //picturePath = decodeFile(picturePath, 1920, 1080);
        Log.d(TAG, "onCreate: " + picturePath);

        if(!imageSent)
            uploadImageFile();

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private String decodeFile(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }
            
            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = "tmp.png";

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    private void uploadImageFile() {
        imageSent = true;
        pbText.setText("Doing the things...");
        Request uploadRequest = new Request("android_upload", "", picturePath, "picture", "picture.jpg", getActivity(), this);
        uploadRequest.executeUploadFile();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public ArrayList<StickerSummary> stickersFromResponse;
    public int counter = 0;
    public int counterLimit = 0;
    public void setCounter(int limit) {
        counter = 0;
        counterLimit = limit;
    }

    public void incrementCounter() {
        counter++;
        if(counter >= counterLimit) {
            showcaseResults();
        }
    }

    StickersAdapter stickersAdapter;
    private void showcaseResults() {
        pb.setVisibility(View.GONE);
        pbText.setVisibility(View.GONE);
        Collections.sort(stickersFromResponse);
        stickersAdapter = new StickersAdapter(getContext(), this, stickersFromResponse, true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();
    }


    @Override
    public void handleResponse(final String response) throws JSONException {
        Log.d(TAG, "handleResponse: " + response);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        setCounter(response.split(",").length);
        stickersFromResponse = new ArrayList<>();

        for(String results : response.split(",")) {
            String result = "";
            if(results.contains("{")) {
                result = results.split(" \"")[1];
            } else if(results.contains("}")) {
                result = results.split("\"")[0];
            } else {
                result = results;
            }
            final String finalResult = result;
            Log.d(TAG, "handleResponse: " + finalResult);
            Log.d(TAG, "handleResponse: " + finalResult.split(":")[0]);

            db.collection("stickers").document(result.split(":")[0]).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                        StickerSummary sticker = new StickerSummary(documentSnapshot.getId(), documentSnapshot.getString("name"),
                                documentSnapshot.getDouble("numberOfUsersWhoHaveThisSticker").intValue(), Float.parseFloat(finalResult.split(":")[1]),
                                hackathons);
                        stickersFromResponse.add(sticker);
                        incrementCounter();
                    }
                }
            });
        }
    }

    @Override
    public void handleError() {
        Log.d(TAG, "handleError: There is a problem");
    }
    
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
