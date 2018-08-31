package scub3d.stickeroverflow;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import scub3d.stickeroverflow.Conversations.Conversation;
import scub3d.stickeroverflow.Conversations.ConversationsFragment;
import scub3d.stickeroverflow.Conversations.SpecificConversationFragment;
import scub3d.stickeroverflow.Hackathons.SpecificHackathonFragment;
import scub3d.stickeroverflow.Hackathons.HackathonsFragment;
import scub3d.stickeroverflow.Organizers.OrganizersFragment;
import scub3d.stickeroverflow.Organizers.SpecificOrganizerFragment;
import scub3d.stickeroverflow.Sponsors.SponsorsFragment;
import scub3d.stickeroverflow.Stickers.SpecificStickerFragment;
import scub3d.stickeroverflow.Stickers.StickersFragment;
import scub3d.stickeroverflow.Users.SpecificUserFragment;
import scub3d.stickeroverflow.Users.UsersFragment;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        HackathonsFragment.OnFragmentInteractionListener,
        SpecificHackathonFragment.OnFragmentInteractionListener,
        OrganizersFragment.OnFragmentInteractionListener,
        SponsorsFragment.OnFragmentInteractionListener,
        SpecificOrganizerFragment.OnFragmentInteractionListener,
        StickersFragment.OnFragmentInteractionListener,
        SpecificStickerFragment.OnFragmentInteractionListener,
        UsersFragment.OnFragmentInteractionListener,
        SpecificUserFragment.OnFragmentInteractionListener,
        ConversationsFragment.OnFragmentInteractionListener,
        SpecificConversationFragment.OnFragmentInteractionListener,
        AccountFragment.OnFragmentInteractionListener,
        TakePictureFromPreviewFragment.OnFragmentInteractionListener,
        GetServerStickerResponseFragment.OnFragmentInteractionListener {

    private static String TAG = "MYTAG";

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private ImageView userPhoto;
    private TextView userDisplayName;
    private TextView userEmail;
    private NavigationView navigationView;

    private BroadcastReceiver statusReceiver;
    private IntentFilter mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = null;
            fragmentClass = MainFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setTitle("Sticker Overflow");
                            onBackPressed();
                        }
                    });
                } else {
                    //show hamburger
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userDisplayName = navigationView.getHeaderView(0).findViewById(R.id.userDisplayName);
        userPhoto = navigationView.getHeaderView(0).findViewById(R.id.userPhoto);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.userEmail);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET},1);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        final String convoOtherUserId = getIntent().getStringExtra("openConversation");
        Log.d(TAG, "onCreate: " + convoOtherUserId);

        if(getIntent().getExtras() != null) {
            for(String key : getIntent().getExtras().keySet()) {
                Log.d(TAG, "onCreate: KEY = " + key);
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "onCreate: VALUE = " + value);
            }
        }

        if(convoOtherUserId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(getFirebaseId()).collection("conversations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for (DocumentSnapshot convoSnapshot : task.getResult()) {
                            if(convoSnapshot.getString("otherUserId").equals(convoOtherUserId)) {
                                Fragment fragment = new SpecificConversationFragment();
                                Bundle args = new Bundle();

                                args.putString("conversationData", new Gson().toJson(convoSnapshot.toObject(Conversation.class)));
                                args.putBoolean("newConversation", false);
                                args.putBoolean("isFromNotification", true);
                                args.putString("otherUserId", convoSnapshot.getString("otherUserId"));

                                fragment.setArguments(args);
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.flContent, fragment, "Conversation:" + convoSnapshot.getId()).addToBackStack(null).commit();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(!getSupportFragmentManager().popBackStackImmediate()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_camera) {
            fragmentClass = TakePictureFromPreviewFragment.class;
        } else if (id == R.id.nav_stickers) {
            fragmentClass = StickersFragment.class;
        } else if (id == R.id.nav_hackathons) {
            fragmentClass = HackathonsFragment.class;
        } else if (id == R.id.nav_organizers) {
            fragmentClass = OrganizersFragment.class;
        } else if (id == R.id.nav_sponsors) {
            fragmentClass = SponsorsFragment.class;
        } else if(id == R.id.nav_users) {
            fragmentClass = UsersFragment.class;
        } else if (id == R.id.nav_chat) {
            fragmentClass = ConversationsFragment.class;
        } else if (id == R.id.nav_account) {
            fragmentClass = AccountFragment.class;
        } else if (id == R.id.nav_signin) {
            fragmentClass = MainFragment.class;
            signIn();
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragment.toString().split("\\{")[0]).addToBackStack(null).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) { }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if(mIntent != null) {
            unregisterReceiver(statusReceiver);
            mIntent = null;
        }
        super.onPause();
    }

    public void onDestroy() { super.onDestroy(); }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("ReceivedFCM"));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            pushToken(10000);
                        } else {
                            updateUI(null);
                        }
                    }
                });
    }

    private void pushToken(int delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String token = FirebaseInstanceId.getInstance().getToken();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(getFirebaseId()).update("fcmToken", token).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: Success = " + task.isSuccessful());
                    }
                });
            }
        }, delay);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void revokeAccess() {
        mAuth.signOut();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    public String getFirebaseId() {
        return mAuth.getCurrentUser().getUid();
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            userEmail.setText(user.getEmail());
            userDisplayName.setText(user.getDisplayName());
            new DownloadImageTask(userPhoto).execute(user.getPhotoUrl().toString());
            navigationView.getMenu().findItem(R.id.nav_signin).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_account).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
        } else {
            userEmail.setText("user@gmail.com");
            userDisplayName.setText("User");
            userPhoto.setImageResource(android.R.drawable.sym_def_app_icon);
            navigationView.getMenu().findItem(R.id.nav_signin).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_account).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
        }
    }

    public int counter = 0;
    public int counterLimit = 0;
    public boolean notify = true;

    public void setCounter(int limit) {
        counter = 0;
        counterLimit = limit;
    }

    public void incrementCounter() {
        counter++;
        if(counter >= counterLimit) {
            if(notify) {
                makeNotification();
            }
        }
    }

    private void makeNotification() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Activity mActivity = this;
        db.collection("users").document(notificationIcon).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                new createNotification(mActivity).execute(documentSnapshot.getString("name"), notificationBody, documentSnapshot.getString("photoUrl"), String.valueOf(notificationSentTime));
            }
        });
    }

    private String notificationIcon, notificationBody;
    private long notificationSentTime;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notificationIcon = intent.getStringExtra("icon");
            notificationBody = intent.getStringExtra("body");
            notificationSentTime = intent.getLongExtra("sentTime", 0);
            decideNotification(notificationIcon);
        }
    };

    public void decideNotification(final String senderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(getFirebaseId()).collection("conversations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    setCounter(task.getResult().size());
                    for(DocumentSnapshot conversationSnapshot : task.getResult()) {
                        if(conversationSnapshot.getString("otherUserId").equals(senderId)) {
                            SpecificConversationFragment scf = (SpecificConversationFragment) getSupportFragmentManager().findFragmentByTag("Conversation:" + conversationSnapshot.getId());
                            if (scf != null && scf.isVisible()) {
                                notify = false;
                            }
                        } else {
                            notify = false;
                        }
                        incrementCounter();
                    }
                }
            }
        });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {;
            bmImage.setImageBitmap(result);
            //Log.d(TAG, "onPostExecute: Width = " + bmImage.getWidth() + ", Height = " + bmImage.getHeight());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private class createNotification extends AsyncTask<String, Void, Bitmap> {
        Context ctx;
        Activity activity;
        String message, title, time;
        long sentTime;

        public createNotification(Context context) {
            super();
            this.ctx = context;
        }

        public createNotification(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            title   = params[0];
            message = params[1];
            sentTime = Long.parseLong(params[3]);

            Date mDate = new Date(sentTime);
            DateFormat formatter = new SimpleDateFormat("hh:mm a");
            time = formatter.format(mDate);

            try {
                in = new URL(params[2]).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                return bmp;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("openConversation", notificationIcon);

                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

                String channelId = getString(R.string.default_notification_channel_id);
                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(activity, channelId)
                                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                                .setLargeIcon(result)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setSubText(time)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}


