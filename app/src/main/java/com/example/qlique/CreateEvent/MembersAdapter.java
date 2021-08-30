package com.example.qlique.CreateEvent;
import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.qlique.NewMessageActivity;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.ChatLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
/**
 * Class MembersAdapter.
 * This class responsible of the Members adapter.
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private String[] members; // array of all the members
    /**
     * Constructor.
     * @params Context context,String[] events
     */
    MembersAdapter(Context context,String[] events){
        this.inflater = LayoutInflater.from(context);
        this.members = events;
    }

    @NonNull
    @Override
    /**
     * Configures the ViewHolder
     * @params @NonNull ViewGroup viewGroup, int i
     */
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.member_row_event,viewGroup,false);
        return new ViewHolder(view);
    }
    /**
     * sets the User object's fields from the snapshot
     * @params snapshot - DataSnapshot from Firebase
     * @return Configured User object
     */
    private String getField(DataSnapshot snapshot,String field) {
        Object val =null;
        if (field==null){
            return null;
        }
        val = snapshot.child(field).getValue();
        if(val == null){
            return null;
        }
        else return val.toString();
    }
   private User loadUser(DataSnapshot snapshot){
        User user = new User();
        user.firstName = getField(snapshot,"firstName");
        user.lastName = getField(snapshot,"lastName");
        user.email = getField(snapshot,"email");
        user.city =  getField(snapshot,"city");
        user.gender = getField(snapshot,"gender");
        user.uid = getField(snapshot,"uid");
        user. url = getField(snapshot,"url");
        user.tokenFCM = getField(snapshot,"tokenFCM");
        return user;
    }
    @Override
    /**
     * responsible of the representation of the member fetched from Firebase
     * @params @NonNull ViewHolder viewHolder, int i
     */
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+members[i]);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            /**
             * When data has changed this function loads the data(text,photos) of the member
             * @params @NonNull ViewHolder viewHolder, int i
             */
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = loadUser(dataSnapshot);
                String  url_profile = user.url; // the url of the user's profile picture
                ImageView targetAuthorImageView = viewHolder.itemView.findViewById(R.id.photo_event_new);
                TextView targetAuthor = viewHolder.itemView.findViewById(R.id.member_username);
                //loads the image to the target ImageView using Picasso
                Picasso.get().load(url_profile).into(targetAuthorImageView);
                //sets the Author's username
                targetAuthor.setText(user.firstName+" "+user.lastName);
                ImageView chat = viewHolder.itemView.findViewById(R.id.send_msg_member);
                if (user.uid.equals(FirebaseAuth.getInstance().getUid())){
                    // user can not send to himself/herself a message
                    chat.setVisibility(View.GONE);
                } else {
                    chat.setVisibility(View.VISIBLE);
                    //sets an event listener so by that, when the chat button is clicked, the chat activity starts
                    chat.setOnClickListener(v -> {
                        Intent intent = new Intent(viewHolder.itemView.getContext(), ChatLogActivity.class);
                        intent.putExtra(NewMessageActivity.USER_KEY, user);
                        viewHolder.itemView.getContext().startActivity(intent);
                    });
                }
                viewHolder.itemView.findViewById(R.id.layoutMember).setOnClickListener(v -> {
                    Intent intent = new Intent(viewHolder.itemView.getContext(), ChatLogActivity.class);
                    intent.putExtra(NewMessageActivity.USER_KEY, user);
                    viewHolder.itemView.getContext().startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    /**
     * @return the number of elements in the adapter
     */
    public int getItemCount() {
        return members.length;
    }
    /**
     *class ViewHolder extends RecyclerView.ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        /**
         *Constructor.
         * @params @NonNull View itemView
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
            });
        }
    }

}

