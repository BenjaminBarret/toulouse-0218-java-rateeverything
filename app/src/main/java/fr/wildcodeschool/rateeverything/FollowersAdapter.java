package fr.wildcodeschool.rateeverything;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by wilder on 29/03/18.
 */

public class FollowersAdapter extends ArrayAdapter<FollowersModel> {
    public FollowersAdapter (Context context, ArrayList<FollowersModel> followers) {
        super(context, 0, followers);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        final FollowersModel followers = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_followers, parent, false);
        }

        if (followers != null) {

            if (followers.getPhotouser() != null) {
                if (followers.getPhotouser().equals("1")){
                    ImageView photo = (ImageView) convertView.findViewById(R.id.imageview_photo_followers);
                    Glide.with(parent.getContext()).load(R.drawable.defaultimageuser).into(photo);
                } else {
                    ImageView photo = (ImageView) convertView.findViewById(R.id.imageview_photo_followers);
                    Glide.with(parent.getContext()).load(followers.getPhotouser().toString()).into(photo);
                }
            }
            else if (followers.getPhotouser() == null) {
                ImageView photo = (ImageView) convertView.findViewById(R.id.imageview_photo_followers);
                Glide.with(parent.getContext()).load(R.drawable.defaultimageuser).into(photo);
            }

            if (followers.getUsername().toString() != null) {
                TextView textName = (TextView) convertView.findViewById(R.id.textview_user_name_followers);
                textName.setText(followers.getUsername().toString());
            }

            if (followers.getNbphoto() != 0) {
                TextView textNbPhoto = (TextView) convertView.findViewById(R.id.textview_nb_photo_followers);
                textNbPhoto.setText(followers.getNbphoto() + "");
            }
            else if(followers.getNbphoto() == 0){
                TextView textNbPhoto = (TextView) convertView.findViewById(R.id.textview_nb_photo_followers);
                textNbPhoto.setText(0 + "");
            }

            if (followers.getNbfollowers() != 0) {
                TextView textNbFollowers = (TextView) convertView.findViewById(R.id.textview_nb_followers);
                textNbFollowers.setText(followers.getNbfollowers() + "");
            }
            else if (followers.getNbfollowers() == 0){
                TextView textNbFollowers = (TextView) convertView.findViewById(R.id.textview_nb_followers);
                textNbFollowers.setText(0 + "");
            }
        }

        return convertView;
    }



}
