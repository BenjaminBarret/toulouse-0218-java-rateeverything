package fr.wildcodeschool.rateeverything;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private String mUserID;
    private long mNbPhotoUser;

    private ListView mListViewFollowers;
    private ArrayList<FollowersModel> mFollowers;
    private FollowersAdapter mAdapter;
    private FollowersModel mModel;

    FirebaseDatabase mDatabase;
    FirebaseUser mUser;
    DatabaseReference myRef, refFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_follow);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserID = mUser.getUid();
        myRef = mDatabase.getReference("Users/" + mUserID + "/Profil/");
        refFollowers = mDatabase.getReference("Users/");

        mListViewFollowers = findViewById(R.id.listview_followers);
        mFollowers = new ArrayList<>();
        mAdapter = new FollowersAdapter(this, mFollowers);

        mListViewFollowers.setAdapter(mAdapter);
        // Affichage Followers
        refFollowers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFollowers.clear();
                // Affichage de nos follows
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    mNbPhotoUser = ds.child("Photo").getChildrenCount();
                    final String testFollowers = ds.getKey();
                    mDatabase.getReference("Users").child(testFollowers).child("Profil").child("nbphoto").setValue(mNbPhotoUser);
                    if (!(ds.getKey()).equals(mUserID)){
                        if(dataSnapshot.child(mUserID).child("Followers").child(testFollowers).exists()){
                            Boolean follow = (Boolean) dataSnapshot.child(mUserID).child("Followers").child(testFollowers).getValue();
                            if(follow){
                                mModel = ds.child("Profil").getValue(FollowersModel.class);
                                mFollowers.add(mModel);
                            }
                        }
                    }
                }
                // Affichage de nos anciens follows
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    final String testFollowers = ds.getKey();
                    if (!(ds.getKey()).equals(mUserID)){
                        if(dataSnapshot.child(mUserID).child("Followers").child(testFollowers).exists()){
                            Boolean follow = (Boolean) dataSnapshot.child(mUserID).child("Followers").child(testFollowers).getValue();
                            if(follow == false){
                                mModel = ds.child("Profil").getValue(FollowersModel.class);
                                mFollowers.add(mModel);
                            }
                        }
                    }
                }
                // Affichage des Users non follow
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    final String testFollowers = ds.getKey();
                    if (!(ds.getKey()).equals(mUserID)){
                        if(!dataSnapshot.child(mUserID).child("Followers").child(testFollowers).exists()){
                                mModel = ds.child("Profil").getValue(FollowersModel.class);
                                mFollowers.add(mModel);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mListViewFollowers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                FollowersModel newFollow = (FollowersModel) mListViewFollowers.getItemAtPosition(position);
                String idFollowers = newFollow.getId().toString();
                Intent intentFollowers = new Intent(FollowersActivity.this, ProfilUserActivity.class);
                intentFollowers.putExtra("idprofil", idFollowers);
                startActivity(intentFollowers);
            }
        });


        // -------------------------MENU BURGER DON'T TOUCH--------------------------------


        NavigationView navigationView = findViewById(R.id.nav_follow);
        navigationView.setNavigationItemSelectedListener(this);
        Singleton singleton = Singleton.getsIntance();
        singleton.loadNavigation(navigationView);


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intentHome = new Intent(FollowersActivity.this, MainActivity.class);
            startActivity(intentHome);
        } else if (id == R.id.profil) {
            Intent intentProfil = new Intent(FollowersActivity.this, ProfilUserActivity.class);
            intentProfil.putExtra("idprofil", mUserID);
            startActivity(intentProfil);
        } else if (id == R.id.followers) {
            Intent intentFollowers = new Intent(FollowersActivity.this, FollowersActivity.class);
            startActivity(intentFollowers);
        } else if (id == R.id.disconnect) {
            FirebaseAuth.getInstance().signOut();
            SaveSharedPreference.setUserName(FollowersActivity.this, "");
            Intent goToLoginActivity = new Intent(FollowersActivity.this,LoginActivity.class);
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

}

