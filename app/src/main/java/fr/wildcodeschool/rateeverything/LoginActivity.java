package fr.wildcodeschool.rateeverything;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.util.ArrayList;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    //CONSTANT
    static final int SELECT_IMAGE = 0;
    private static final int REQUEST_TAKE_PHOTO = 11;
    private static final String ID_PROFIL = "idprofil";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;

    //Widget
    private Button mButtonSignIn, mButtonCreateAccount, mButtonValidLogin, mButtonValidCreate;
    private EditText mEditPseudo, mEditPassword, mEditMail;
    private TextView mTextPseudo, mTextMail, mTextPassword, mTextChangeAvatar;
    private ProgressBar mProgressBarLoading;
    private ImageView mImageLogo;
    private ImageView mImageAvatar;
    private TextView mTextChooseConnection;

    //Intent
    private Intent mGoToMainActivity;

    //Photo
    private Uri mUrlImage;
    private String mCurrentPhotoPath;
    private Uri mPhotoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRef = mFirebaseDatabase.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mGoToMainActivity = new Intent(LoginActivity.this, MainActivity.class);

        initWidgets();

        if (isSharedPreference()) {

            startActivity(mGoToMainActivity);
            finish();
        }


        //Show Widget Create Account
        mButtonCreateAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                initWidgetRegister();
            }
        });

        //Register
        mButtonValidCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                createAccount();
            }
        });

        //Show Widget Sign In
        mButtonSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                initWidgetSignIn();

            }
        });

        //Sign In
        mButtonValidLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                signIn();
            }
        });

        //Choose Avatar
        mTextChangeAvatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showPickImageDialog();
            }
        });

        //Return
        mTextChooseConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                initWidgetChoose();

            }

        });


    }
    /*
    ---------------------------------CreateAnAccount--------------------------------------
     */
    public void createAccount() {

        String mail = mEditMail.getText().toString().trim();
        String pass = mEditPassword.getText().toString().trim();

        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)) {

            Toast.makeText(LoginActivity.this, R.string.bothValues, Toast.LENGTH_SHORT).show();
        }
        else if(pass.length() < 6) {

            Toast.makeText(LoginActivity.this, R.string.passwordNeed, Toast.LENGTH_SHORT).show();
        }
        else {

            mProgressBarLoading.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        mProgressBarLoading.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, R.string.authentificatinFailed, Toast.LENGTH_SHORT).show();
                    }
                    else {

                        String mail = mEditMail.getText().toString().trim();
                        String pseudo = mEditPseudo.getText().toString().trim();
                        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                        final String userID = mCurrentUser.getUid();
                        mRef.child(userID).child("Profil").child("id").setValue(userID);
                        mRef.child(userID).child("Profil").child("mail").setValue(mail);
                        mRef.child(userID).child("Profil").child("username").setValue(pseudo);
                        mRef.child(userID).child("Profil").child("nbfollowers").setValue(0);
                        mRef.child(userID).child("Profil").child("nbphoto").setValue(0);
                        mRef.child(userID).child("Profil").child("photobackground").setValue("1");

                        if (mPhotoURI == null) {
                            mRef.child(userID).child("Profil").child("photouser").setValue("1");
                        } else {
                            //enregistre dans firebaseStorage
                            StorageReference riverRef = mStorageRef.child("PhotoUser").child(mPhotoURI.getLastPathSegment());;

                            riverRef.putFile(mPhotoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    String url = downloadUrl.toString();

                                    mRef.child(userID).child("Profil").child("photouser").setValue(url);
                                    Toast.makeText(LoginActivity.this, R.string.Ok, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    Toast.makeText(LoginActivity.this,"Upload is " + progress + "% done", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        SaveSharedPreference.setUserName(LoginActivity.this, mail);
                        Singleton singleton = Singleton.getsIntance();
                        singleton.setListener(new Singleton.LoadListener() {
                            @Override
                            public void onListUpdate(ArrayList<MainPhotoModel> photo) {
                            }
                            @Override
                            public void onUserLoading() {

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                mProgressBarLoading.setVisibility(View.GONE);

                            }
                        });
                        singleton.loadUser();
                        finish();

                    }

                }
            });
        }
    }

    /*
    -------------------------------SignIn---------------------------------------------
     */

    public void signIn() {

        String mail = mEditMail.getText().toString().trim();
        String pass = mEditPassword.getText().toString().trim();
        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)) {

            Toast.makeText(LoginActivity.this, R.string.bothValues, Toast.LENGTH_SHORT).show();
        } else {

            mProgressBarLoading.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String userID = mCurrentUser.getUid();

                        mGoToMainActivity.putExtra(ID_PROFIL, userID);

                        String mail = mEditMail.getText().toString().trim();
                        SaveSharedPreference.setUserName(LoginActivity.this, mail);

                        Singleton singleton = Singleton.getsIntance();
                        singleton.setListener(new Singleton.LoadListener() {
                            @Override
                            public void onListUpdate(ArrayList<MainPhotoModel> photo) {
                            }
                            @Override
                            public void onUserLoading() {

                                LoginActivity.this.startActivity(mGoToMainActivity);
                                mProgressBarLoading.setVisibility(View.GONE);

                            }
                        });
                        singleton.loadUser();
                        finish();


                    } else {
                        mProgressBarLoading.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, R.string.incorrectUserPassword, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /*
    -----------------------------------AvatarMethod-------------------------------------------------
     */

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
        String timeStamp = new SimpleDateFormat("ddMMyyy_HHmmss").format(new Date());
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

    private void showPickImageDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(LoginActivity.this);
        builderSingle.setTitle(R.string.choisissez);

        final String [] items = new String[] {"Gallerie", "Appareil photo"};
        final Integer[] icons = new Integer[] {R.drawable.gallery, R.drawable.camera_moto_icon};
        ListAdapter adapter = new ArrayAdapterWithIcon(LoginActivity.this, items, icons);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    mPhotoURI = selectedImage;
                    mImageLogo.setVisibility(View.GONE);
                    mImageAvatar.setVisibility(View.VISIBLE);
                    Glide.with(LoginActivity.this).load(mPhotoURI).into(mImageAvatar);
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    mImageLogo.setVisibility(View.GONE);
                    mImageAvatar.setVisibility(View.VISIBLE);
                    Glide.with(LoginActivity.this).load(mPhotoURI).into(mImageAvatar);
                }
                break;
        }
    }

    /*
    -----------------------------------Firebase-----------------------------------------------------
     */



    /*
    ------------------------------SharedPreference--------------------------------------------------
     */
    private boolean isSharedPreference() {

        if(SaveSharedPreference.getUserName(LoginActivity.this).length() != 0)
        {
            // call Login Activity
            return true;
        }
        return false;
    }

    /*
    --------------------------------WidgetsMethod---------------------------------------------------
     */

    private void initWidgets() {

        mButtonSignIn = findViewById(R.id.button_sign_in);
        mButtonCreateAccount = findViewById(R.id.button_create_account);
        mButtonValidLogin = findViewById(R.id.button_valid_login);
        mButtonValidCreate = findViewById(R.id.button_valid_create);
        mEditPseudo = findViewById(R.id.edit_text_pseudo);
        mEditMail = findViewById(R.id.edit_text_mail);
        mEditPassword = findViewById(R.id.edit_text_password);
        mTextPseudo = findViewById(R.id.text_view_pseudo);
        mTextMail = findViewById(R.id.text_view_mail);
        mTextPassword = findViewById(R.id.text_view_password);
        mTextChangeAvatar = findViewById(R.id.text_view_chose_avatar);
        mProgressBarLoading = findViewById(R.id.progress_bar_load);
        mImageLogo = findViewById(R.id.image_view_logo);
        mImageAvatar = findViewById(R.id.image_view_avatar);
        mTextChooseConnection = findViewById(R.id.text_view_register_signIn);
    }

    private void initWidgetSignIn() {

        mButtonSignIn.setVisibility(View.GONE);
        mButtonCreateAccount.setVisibility(View.GONE);
        mTextPseudo.setVisibility(View.INVISIBLE);
        mTextMail.setVisibility(View.VISIBLE);
        mTextPassword.setVisibility(View.VISIBLE);
        mEditPseudo.setVisibility(View.INVISIBLE);
        mEditMail.setVisibility(View.VISIBLE);
        mEditPassword.setVisibility(View.VISIBLE);
        mButtonValidLogin.setVisibility(View.VISIBLE);
        mTextChooseConnection.setVisibility(View.VISIBLE);
    }

    private void initWidgetRegister() {

        mButtonSignIn.setVisibility(View.GONE);
        mButtonCreateAccount.setVisibility(View.GONE);
        mTextPseudo.setVisibility(View.VISIBLE);
        mTextMail.setVisibility(View.VISIBLE);
        mTextPassword.setVisibility(View.VISIBLE);
        mEditPseudo.setVisibility(View.VISIBLE);
        mEditMail.setVisibility(View.VISIBLE);
        mEditPassword.setVisibility(View.VISIBLE);
        mButtonValidCreate.setVisibility(View.VISIBLE);
        mTextChangeAvatar.setVisibility(View.VISIBLE);
        mTextChooseConnection.setVisibility(View.VISIBLE);
    }

    private void initWidgetChoose() {
        mButtonSignIn.setVisibility(View.VISIBLE);
        mButtonCreateAccount.setVisibility(View.VISIBLE);
        mTextPseudo.setVisibility(View.GONE);
        mTextMail.setVisibility(View.GONE);
        mTextPassword.setVisibility(View.GONE);
        mEditPseudo.setVisibility(View.GONE);
        mEditMail.setVisibility(View.GONE);
        mEditPassword.setVisibility(View.GONE);
        mButtonValidCreate.setVisibility(View.GONE);
        mTextChangeAvatar.setVisibility(View.GONE);
        mButtonValidLogin.setVisibility(View.GONE);
        mTextChooseConnection.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        Intent goToLoginActivity = new Intent(LoginActivity.this, LoginActivity.class);
        startActivity(goToLoginActivity);

    }

}

