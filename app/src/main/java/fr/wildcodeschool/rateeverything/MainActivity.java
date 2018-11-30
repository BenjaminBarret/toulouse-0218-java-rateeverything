package fr.wildcodeschool.rateeverything;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ImageView mImgViewUserHeader;
    private Boolean mTestFollow;

    private String mImgUser, mUserID;

    private static final String ID_PROFIL = "idprofil";

    //ListeView
    private ListView mListview;
    private ArrayList<MainPhotoModel> mPhotoList;

    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef, mMyRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImgViewUserHeader = mDrawerLayout.findViewById(R.id.img_header_user);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserID = mCurrentUser.getUid();
        mMyRef = mFirebaseDatabase.getReference("Users/" + mUserID + "/Profil/");
        mRef = mFirebaseDatabase.getReference("Users/");


        mListview = findViewById(R.id.listview_photo_main);
        Singleton singleton = Singleton.getsIntance();
        mPhotoList = singleton.getmListPrincipal();
        final MainPhotoAdapter adapter = new MainPhotoAdapter(this, mPhotoList);

        // Affichage Liste view
        mListview.setAdapter(adapter);

        singleton.setListener(new Singleton.LoadListener() {
            @Override
            public void onListUpdate(ArrayList<MainPhotoModel> photo) {
                mPhotoList = photo;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onUserLoading() {

            }
        });


        // Bouton Flotant
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPhotoActivity.class);
                startActivity(intent);
            }
        });
        // Menu Burger
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        singleton.loadNavigation(navigationView);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent intentHome = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intentHome);
        } else if (id == R.id.profil) {
            Intent intentProfil = new Intent(MainActivity.this, ProfilUserActivity.class);
            intentProfil.putExtra(ID_PROFIL, mUserID);
            startActivity(intentProfil);
        } else if (id == R.id.followers) {
            Intent intentFollowers = new Intent(MainActivity.this, FollowersActivity.class);
            startActivity(intentFollowers);
        } else if (id == R.id.disconnect){
            FirebaseAuth.getInstance().signOut();
            SaveSharedPreference.setUserName(MainActivity.this, "");
            Intent goToLoginActivity = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(goToLoginActivity);
            finish();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

}
