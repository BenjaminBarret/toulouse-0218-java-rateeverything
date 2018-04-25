package fr.wildcodeschool.rateeverything;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserPhoto extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private String mUserID;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mPhotoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_photo);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_photo);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView tvTitre = findViewById(R.id.text_titre_photo);
        final TextView tvDescription = findViewById(R.id.text_description_photo);
        final ImageView ivPhoto = findViewById(R.id.imageview_photo);
        RatingBar ratingBar = findViewById(R.id.bar_modif_note);

        final String profilId = getIntent().getStringExtra("idprofil");
        final String idPhoto = getIntent().getStringExtra("keyphoto");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserID = mCurrentUser.getUid();
        mPhotoRef = database.getReference("Users/" + profilId + "/Photo/" + idPhoto);

        //Supprimer ses photos
        Button boutonSupp = findViewById(R.id.button_suppression_photo);
        if(mUserID.equals(profilId)){
            boutonSupp.setVisibility(View.VISIBLE);
            boutonSupp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // mPhotoRef.removeValue();
                    AlertDialog.Builder popup = new AlertDialog.Builder(UserPhoto.this);
                    popup.setTitle("Supprimer photo");
                    popup.setMessage("Es tu sur de vouloir supprimer cette photo ?");
                    popup.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mPhotoRef.removeValue();
                            Toast.makeText(UserPhoto.this, "Photo supprimée", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(UserPhoto.this, ProfilUserActivity.class);
                            intent.putExtra("idprofil", profilId);
                            startActivity(intent);
                        }
                    });
                    popup.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    popup.show();
                }
            });
        }

        database.getReference("Users").child(profilId).child("Photo").child(idPhoto)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MainPhotoModel laphoto = (MainPhotoModel) dataSnapshot.getValue(MainPhotoModel.class);
                if(laphoto.getPhoto() != null){
                    Glide.with(UserPhoto.this).load(dataSnapshot.child("photo").getValue()).into(ivPhoto);
                }
                tvTitre.setText(laphoto.getTitle());
                tvDescription.setText(laphoto.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // -----------MENU BURGER DON'T TOUCH FOR THE MOMENT------------------

        NavigationView navigationViewPhoto = (NavigationView) findViewById(R.id.nav_view_photo);
        navigationViewPhoto.setNavigationItemSelectedListener(this);

        // TODO reglage menu burger

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intentHome = new Intent(UserPhoto.this, MainActivity.class);
            startActivity(intentHome);
        } else if (id == R.id.profil) {
            Intent intentProfil = new Intent(UserPhoto.this, ProfilUserActivity.class);
            intentProfil.putExtra("idprofil", mUserID);
            startActivity(intentProfil);
        } else if (id == R.id.followers) {
            Intent intentFollowers = new Intent(UserPhoto.this, FollowersActivity.class);
            startActivity(intentFollowers);
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
