package fr.wildcodeschool.rateeverything;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wilder on 27/03/18.
 */

public class MainPhotoAdapter extends ArrayAdapter<MainPhotoModel> {

    public MainPhotoAdapter(Context context, ArrayList<MainPhotoModel> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final MainPhotoModel photoModel = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_photos, parent, false);
        }
        final ImageView photo = convertView.findViewById(R.id.image_photo);
        if (photoModel.getPhoto() != null) {
            Glide.with(getContext()).load(photoModel.getPhoto()).into(photo);
        }
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyphoto = photoModel.getKeyphoto();
                String iduserphoto = photoModel.getIduser();
                Intent intentphoto = new Intent(parent.getContext(), UserPhoto.class);
                intentphoto.putExtra("keyphoto", keyphoto);
                intentphoto.putExtra("idprofil", iduserphoto);
                parent.getContext().startActivity(intentphoto);
            }
        });

        FollowersModel user = (FollowersModel) photoModel.getFollowersModel();

        TextView textTitle = (TextView) convertView.findViewById(R.id.text_user_name_pub);
        textTitle.setText(photoModel.getTitle());

        TextView textDatePub = (TextView) convertView.findViewById(R.id.text_date_pub);
        long timestamp = -photoModel.getTimestamp();
        textDatePub.setText(getDate(timestamp));

        TextView textUserName = convertView.findViewById(R.id.textview_name_user_list);
        textUserName.setText(user.getUsername());

        ImageView ivPhotoUser = convertView.findViewById(R.id.imageview_photo_user_list);

        if (user.getPhotouser().equals("1")) {
            Glide.with(getContext()).load(R.drawable.defaultimageuser).apply(RequestOptions.circleCropTransform()).into(ivPhotoUser);
        } else {
            Glide.with(getContext()).load(user.getPhotouser()).apply(RequestOptions.circleCropTransform()).into(ivPhotoUser);
        }


        TextView note = (TextView) convertView.findViewById(R.id.text_note);

        int valeurnote = photoModel.getTotalnote() / photoModel.getNbnote();
        note.setText(valeurnote + " / 5" );

        RatingBar ratingBar = convertView.findViewById(R.id.rating_bar);
        ratingBar.setRating(valeurnote);

        return convertView;
    }

    private String getDate(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return null;
        }
    }
}
