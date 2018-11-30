package fr.wildcodeschool.rateeverything;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProfilUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    final static int SELECT_IMAGE = 1;
    final static int REQUEST_TAKE_PHOTO = 2;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private DatabaseReference mProfil, mRefUserPhoto, mRefFollowers;
    private StorageReference mStorageRef;

    private ImageView mPhoto;
    private TextView mNbFollowers, mNbPhoto, mUserName;
    private String mUserID;
    private long mNbPhotouser, mNbFollowersUsers;

    private Uri mPhotoURI;
    private String mCurrentPhotoPath;


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private GridView mUserPhoto;
    private ArrayList<MainPhotoModel> mUserProfil;
    private ProfilUserGridAdapter mUserAdapter;
    private MainPhotoModel mModelPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_user);

        mPhoto = findViewById(R.id.profil_photo_user);
        mNbFollowers = findViewById(R.id.item_followers);
        mNbPhoto = findViewById(R.id.item_pictures);
        mUserName = findViewById(R.id.edit_name_user);
        Button boutonModifier = findViewById(R.id.button_modify_profil);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mUserID = mUser.getUid();

        final String profilId = getIntent().getStringExtra("idprofil");
        if (mUserID.equals(profilId)) {

            mProfil = mDatabase.getReference("Users/" + mUserID + "/Profil/");

            // Affichage info user
            mDatabase.getReference("Users").child(mUserID).child("Photo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mNbPhotouser = dataSnapshot.getChildrenCount();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            mProfil.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("username").getValue() != null) {
                        String stringName = (String) dataSnapshot.child("username").getValue();
                        mUserName.setText(stringName);
                    }
                    if (dataSnapshot.child("nbfollowers").getValue() != null) {
                        String stringNbFollowers = dataSnapshot.child("nbfollowers").getValue().toString();
                        mNbFollowers.setText(stringNbFollowers);
                    }
                    if (dataSnapshot.child("nbphoto").getValue() != null) {
                        mNbPhoto.setText(mNbPhotouser + "");
                    }
                    if (dataSnapshot.child("photouser").getValue() != null) {
                        String stringUrl = (String) dataSnapshot.child("photouser").getValue();
                        if (stringUrl.equals("1")){
                            Glide.with(ProfilUserActivity.this)
                                    .load(R.drawable.defaultimageuser)
                                    .into(mPhoto);
                        }
                        else {
                            Glide.with(ProfilUserActivity.this)
                                    .load(stringUrl)
                                    .into(mPhoto);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_user);
            mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
            mDrawerLayout.addDrawerListener(mToggle);
            mToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mRefUserPhoto = FirebaseDatabase.getInstance().getReference().child("Users/"+ mUserID +"/Photo/");
            mUserPhoto = findViewById(R.id.grid_view_user);
            mUserProfil = new ArrayList<>();
            mUserAdapter = new ProfilUserGridAdapter(this, mUserProfil);

            mRefUserPhoto.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserProfil.clear();
                    for(DataSnapshot dsUser: dataSnapshot.getChildren()){
                        mModelPhoto = dsUser.getValue(MainPhotoModel.class);
                        mUserProfil.add(mModelPhoto);
                        mUserPhoto.setAdapter(mUserAdapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mUserPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainPhotoModel laPhoto = (MainPhotoModel) mUserPhoto.getItemAtPosition(position);
                    Intent goToUserPhoto = new Intent(ProfilUserActivity.this, UserPhoto.class);
                    goToUserPhoto.putExtra("keyphoto", laPhoto.getKeyphoto().toString());
                    goToUserPhoto.putExtra("idprofil", mUserID);
                    startActivity(goToUserPhoto);

                }
            });

        }
        else {
                mProfil = mDatabase.getReference("Users/" + profilId + "/Profil/");
                boutonModifier.setVisibility(View.INVISIBLE);
                final Button boutonAddFollowers = findViewById(R.id.button_add_followers);
                boutonAddFollowers.setVisibility(View.VISIBLE);


                // Fonction Follow
                mDatabase.getReference("Users").child(profilId).child("Profil").child("nbfollowers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mNbFollowersUsers = (long) dataSnapshot.getValue();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                mRefFollowers = mDatabase.getReference("Users/" + mUserID + "/Followers/" + profilId);
                mRefFollowers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Boolean verifFollowers = (Boolean) dataSnapshot.getValue();
                            if (verifFollowers) {
                                boutonAddFollowers.setText(R.string.ne_plus_suivre);
                                boutonAddFollowers.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRefFollowers.setValue(false);
                                        boutonAddFollowers.setText(R.string.suivre_cette_personne);
                                        mDatabase.getReference("Users").child(profilId).child("Profil").child("nbfollowers").setValue(mNbFollowersUsers - 1);
                                    }
                                });
                            }
                            else {
                                boutonAddFollowers.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRefFollowers.setValue(true);
                                        boutonAddFollowers.setText(R.string.ne_plus_suivre);
                                        mDatabase.getReference("Users").child(profilId).child("Profil").child("nbfollowers").setValue(mNbFollowersUsers + 1);
                                    }
                                });
                            }
                        }
                        else {
                            boutonAddFollowers.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mRefFollowers.setValue(true);
                                    boutonAddFollowers.setText(R.string.ne_plus_suivre);
                                    mDatabase.getReference("Users").child(profilId).child("Profil").child("nbfollowers").setValue(mNbFollowersUsers + 1);
                                }
                            });
                        }
                        mUserPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                MainPhotoModel laPhoto = (MainPhotoModel) mUserPhoto.getItemAtPosition(position);
                                Intent goToUserPhoto = new Intent(ProfilUserActivity.this, UserPhoto.class);
                                goToUserPhoto.putExtra("keyphoto", laPhoto.getKeyphoto().toString());
                                goToUserPhoto.putExtra("idprofil", profilId);
                                startActivity(goToUserPhoto);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                // affichage info user
                mDatabase.getReference("Users").child(profilId).child("Photo").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mNbPhotouser = dataSnapshot.getChildrenCount();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                mProfil.addValueEventListener(new ValueEventListener() {
                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("username").getValue() != null) {
                            String stringName = (String) dataSnapshot.child("username").getValue();
                            mUserName.setText(stringName);
                        }
                        if (dataSnapshot.child("nbfollowers").getValue() != null) {
                            String stringNbFollowers = dataSnapshot.child("nbfollowers").getValue().toString();
                            mNbFollowers.setText(stringNbFollowers);
                        }
                        else if((dataSnapshot.child("nbfollowers").getValue() == null)) {
                            mNbFollowers.setText("" + 0);
                        }
                        if (dataSnapshot.child("nbphoto").getValue() != null) {
                            mNbPhoto.setText(mNbPhotouser + "");
                        }
                        else if(dataSnapshot.child("nbphoto").getValue() == null){
                            mNbPhoto.setText("" + 0);
                        }
                        if (dataSnapshot.child("photouser").getValue() != null) {
                            String stringUrl = (String) dataSnapshot.child("photouser").getValue();
                            if (stringUrl.equals("1")){
                                mPhoto.setImageResource(R.drawable.defaultimageuser);
                            }
                            else {
                                Glide.with(ProfilUserActivity.this)
                                        .load(stringUrl)
                                        .into(mPhoto);
                            }
                        }
                        else if(dataSnapshot.child("photouser").getValue() == null){
                            Glide.with(ProfilUserActivity.this)
                                    .load(R.drawable.defaultimageuser)
                                    .into(mPhoto);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_user);
                mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
                mDrawerLayout.addDrawerListener(mToggle);
                mToggle.syncState();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mRefUserPhoto = FirebaseDatabase.getInstance().getReference().child("Users/" + profilId + "/Photo/");
            mUserPhoto = findViewById(R.id.grid_view_user);
            mUserProfil = new ArrayList<>();
            mUserAdapter = new ProfilUserGridAdapter(this, mUserProfil);

            mRefUserPhoto.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserProfil.clear();
                    for(DataSnapshot dsUser: dataSnapshot.getChildren()){
                        mModelPhoto = dsUser.getValue(MainPhotoModel.class);
                        mUserProfil.add(mModelPhoto);
                        mUserPhoto.setAdapter(mUserAdapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_user);
        navigationView.setNavigationItemSelectedListener(this);
        Singleton singleton = Singleton.getsIntance();
        singleton.loadNavigation(navigationView);
    }

    // -----------------------MENU BURGER DON'T TOUCH PLEASE !!!--------------------------

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intentHome = new Intent(ProfilUserActivity.this, MainActivity.class);
            startActivity(intentHome);
        } else if (id == R.id.profil) {
            Intent intentProfil = new Intent(ProfilUserActivity.this, ProfilUserActivity.class);
            intentProfil.putExtra("idprofil", mUserID);
            startActivity(intentProfil);
        } else if (id == R.id.followers) {
            Intent intentFollowers = new Intent(ProfilUserActivity.this, FollowersActivity.class);
            startActivity(intentFollowers);
        } else if (id == R.id.disconnect) {
            FirebaseAuth.getInstance().signOut();
            SaveSharedPreference.setUserName(ProfilUserActivity.this, "");
            Intent goToLoginActivity = new Intent(ProfilUserActivity.this,LoginActivity.class);
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


    //-----------------------------------Button change Image profil---------------------------------

    public void changeImageProfil(View view){

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ProfilUserActivity.this);
        builderSingle.setTitle(R.string.choisissez);

        final String [] items = new String[] {"Gallerie", "Appareil photo"};
        final Integer[] icons = new Integer[] {R.drawable.gallery, R.drawable.camera_moto_icon};
        ListAdapter adapter = new ArrayAdapterWithIcon(ProfilUserActivity.this, items, icons);

        builderSingle.setNegativeButton(
                R.string.annuler,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                adapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto, SELECT_IMAGE);
                                break;

                            case 1:
                                dispatchTakePictureIntent();
                                break;
                        }
                    }
                });
        builderSingle.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mPhotoURI = FileProvider.getUriForFile(this,
                        "fr.wildcodeschool.rateeverything.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    mPhotoURI = selectedImage;
                    Glide.with(ProfilUserActivity.this).load(mPhotoURI).into(mPhoto);
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    Glide.with(ProfilUserActivity.this).load(mPhotoURI).into(mPhoto);
                }else if (resultCode != RESULT_OK){
                    return;
                }
                break;
        }
        if (resultCode == RESULT_OK) {
            mProfil = mDatabase.getReference("Users/" + mUserID + "/Profil/");
            StorageReference riverRef = mStorageRef.child("PhotoUser").child(mPhotoURI.getLastPathSegment());
            riverRef.putFile(mPhotoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String url = downloadUrl.toString();
                    mProfil.child("photouser").setValue(url);
                }
            });
            Intent intentProfil = new Intent(ProfilUserActivity.this, ProfilUserActivity.class);
            intentProfil.putExtra("idprofil", mUserID);
            startActivity(intentProfil);
        }

    }



    //---------------------------------------------------------------------------------------------
}
