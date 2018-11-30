package fr.wildcodeschool.rateeverything;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhotoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ImageView mImagePhoto;
    private String mIDUser;
    static final int CAM_REQUEST = 0;
    static final int SELECT_IMAGE = 1;
    private Uri mSelectedImage = null;
    private Bitmap mBitmap = null;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private FirebaseUser mCurrentUser;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private ImageView mImgViewUserHeader;
    private Intent mGoToMainActivity;
    private RatingBar mNoteBar;
    private ProgressBar mProgressBar;

    private static final int REQUEST_TAKE_PHOTO = 11;

    //Photo
    private Uri mUrlImage;
    private Uri mPhotoURI;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_add_photo);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImgViewUserHeader = mDrawerLayout.findViewById(R.id.img_header_user);

        mGoToMainActivity = new Intent(AddPhotoActivity.this,MainActivity.class);

        mDatabase = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mIDUser = mCurrentUser.getUid();

        mRef = mDatabase.getReference("Users/" + mIDUser + "/Photo/");

        mImagePhoto = (ImageView) findViewById(R.id.iv_photo);
        TextView tvTitle = findViewById(R.id.et_title_img);
        TextView tvDescription = findViewById(R.id.et_description_img);

        mNoteBar = findViewById(R.id.rating_bar_first_note);
        mProgressBar = findViewById(R.id.progress_bar_photo);

        mImagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickImageDialog();
            }
        });

        NavigationView navigationViewAddPhoto = findViewById(R.id.nav_view_add_photo);
        navigationViewAddPhoto.setNavigationItemSelectedListener(this);
        Singleton singleton = Singleton.getsIntance();
        singleton.loadNavigation(navigationViewAddPhoto);

    }

    private void showPickImageDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(AddPhotoActivity.this);
        builderSingle.setTitle(R.string.choisissez);

        final String [] items = new String[] {"Gallerie", "Appareil photo"};
        final Integer[] icons = new Integer[] {R.drawable.gallery, R.drawable.camera_moto_icon};
        ListAdapter adapter = new ArrayAdapterWithIcon(AddPhotoActivity.this, items, icons);

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

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    mImagePhoto.setBackgroundResource(android.R.color.transparent);
                    Glide.with(AddPhotoActivity.this).load(mPhotoURI).into(mImagePhoto);
                }
                break;
            case SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    mSelectedImage = data.getData();
                    mPhotoURI = mSelectedImage;
                    Glide.with(AddPhotoActivity.this).load(mPhotoURI).into(mImagePhoto);
                }
                break;
        }

        final Button addImage = findViewById(R.id.button_share_img);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText tvTitle = findViewById(R.id.et_title_img);
                EditText tvDescription = findViewById(R.id.et_description_img);
                final String titleValue = tvTitle.getText().toString();
                final String descriptionValue = tvDescription.getText().toString();

                StorageReference riverRef = mStorageRef.child("Image").child(mPhotoURI.getLastPathSegment());
                addImage.setEnabled(false);

                riverRef.putFile(mPhotoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String url = downloadUrl.toString();
                        long date = new Date().getTime();
                        String keyPhoto = mRef.push().getKey().toString();
                        MainPhotoModel mainPhotoModel = new MainPhotoModel(descriptionValue, mCurrentUser.getUid(), keyPhoto ,1, url, - date, titleValue, Math.round(mNoteBar.getRating()));
                        mRef.child(keyPhoto).setValue(mainPhotoModel);
                        mRef.child(keyPhoto).child("idvotant").child(mIDUser).setValue(Math.round(mNoteBar.getRating()));
                        mProgressBar.setVisibility(View.GONE);
                        startActivity(mGoToMainActivity);
                        addImage.setEnabled(true);
                        Toast.makeText(AddPhotoActivity.this, R.string.photo_ajoutée, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPhotoActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                        addImage.setEnabled(true);
                    }
                });

            }
        });

        // -------------------------MENU BURGER DON'T TOUCH--------------------------------

    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intentHome = new Intent(AddPhotoActivity.this, MainActivity.class);
            startActivity(intentHome);
        } else if (id == R.id.profil) {
            Intent intentProfil = new Intent(AddPhotoActivity.this, ProfilUserActivity.class);
            intentProfil.putExtra("idprofil", mIDUser);
            startActivity(intentProfil);
        } else if (id == R.id.followers) {
            Intent intentFollowers = new Intent(AddPhotoActivity.this, FollowersActivity.class);
            startActivity(intentFollowers);
        } else if (id == R.id.disconnect) {
            FirebaseAuth.getInstance().signOut();
            SaveSharedPreference.setUserName(AddPhotoActivity.this, "");
            Intent goToLoginActivity = new Intent(AddPhotoActivity.this,LoginActivity.class);
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









