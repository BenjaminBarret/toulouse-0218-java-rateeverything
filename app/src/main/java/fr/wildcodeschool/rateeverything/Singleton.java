package fr.wildcodeschool.rateeverything;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wilder on 25/04/18.
 */

public class Singleton {
    public static Singleton sIntance = null;

    public Boolean mTestFollow;
    public ArrayList<MainPhotoModel> mListPrincipal = new ArrayList<>();
    public LoadListener mListener = null;
    private FollowersModel mUser = null;

    public Singleton() {
        loadUser();
    }

    public static Singleton getsIntance() {
        if (sIntance == null) {
            sIntance = new Singleton();
        }
        return sIntance;
    }

    public ArrayList<MainPhotoModel> getmListPrincipal() {
        return mListPrincipal;
    }

    public void loadUser() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String userId = user.getUid();
            DatabaseReference userRef = database.getReference("Users").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Profil").exists()) {
                        mUser = dataSnapshot.child("Profil").getValue(FollowersModel.class);
                        loadList();
                        if (mListener != null){
                            mListener.onUserLoading();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public FollowersModel getUser() {
        return mUser;
    }

    public void loadList() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        if (userFirebase != null) {
            final String userId = userFirebase.getUid();
            DatabaseReference listRef = database.getReference("Users/");
            listRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mListPrincipal.clear();
                    Map<Long, MainPhotoModel> sortedPhotoList = new TreeMap<>();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        final String testFollowers = userSnapshot.getKey();
                        DataSnapshot userSnap = userSnapshot.child("Profil");
                        FollowersModel userPhoto = userSnap.getValue(FollowersModel.class);
                        mTestFollow = false;
                        if (testFollowers.equals(userId)) {
                            for (DataSnapshot photoSnapshot : userSnapshot.child("Photo").getChildren()) {
                                MainPhotoModel mObjetPhoto = photoSnapshot.getValue(MainPhotoModel.class);
                                mObjetPhoto.setFollowersModel(userPhoto);
                                sortedPhotoList.put(mObjetPhoto.getTimestamp(), mObjetPhoto);
                            }

                        }
                        if (dataSnapshot.child(userId).child("Followers").child(testFollowers).exists()) {
                            mTestFollow = (Boolean) dataSnapshot.child(userId).child("Followers").child(testFollowers).getValue();
                            if (mTestFollow) {
                                for (DataSnapshot photoSnapshot : userSnapshot.child("Photo").getChildren()) {
                                    MainPhotoModel mObjetPhoto = photoSnapshot.getValue(MainPhotoModel.class);
                                    mObjetPhoto.setFollowersModel(userPhoto);
                                    sortedPhotoList.put(mObjetPhoto.getTimestamp(), mObjetPhoto);
                                }
                            }
                        }
                    }
                    for (Map.Entry<Long, MainPhotoModel> entry : sortedPhotoList.entrySet()) {
                        mListPrincipal.add(entry.getValue());
                    }

                    if (mListener != null) {
                        mListener.onListUpdate(mListPrincipal);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void setListener(LoadListener listener) {
        this.mListener = listener;
    }

    public interface LoadListener {
        void onListUpdate(ArrayList<MainPhotoModel> photo);
        void onUserLoading();
    }

    public void loadNavigation(NavigationView navigationView) {
        FollowersModel user = getUser();
        View hView =  navigationView.getHeaderView(0);
        TextView tvUserName = hView.findViewById(R.id.textview_name_header);
        TextView tvuserEmail = hView.findViewById(R.id.textview_mail_header);
        ImageView ivUserAvatar = hView.findViewById(R.id.img_header_user);
        tvuserEmail.setText(user.getMail());
        tvUserName.setText(user.getUsername());
        if (user.getPhotouser() == null){
            Glide.with(hView.getContext())
                        .load(R.drawable.defaultimageuser)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivUserAvatar);
        }
        else if(user.getPhotouser().equals("1")){
            Glide.with(hView.getContext())
                    .load(R.drawable.defaultimageuser)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserAvatar);
        }
        else {
            Glide.with(hView.getContext())
                        .load(user.getPhotouser())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivUserAvatar);
        }


    }

}
