package com.example.qlique.Services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.example.qlique.NewMessageActivity;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.ChatLogActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;


public class FirebaseNotificationService extends FirebaseMessagingService {
    private String  CHANNEL_ID = "1000";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();
            String title = map.get("title");
            String message = map.get("message");
            String uidOfSender = map.get("SenderUid");
            Log.d("TAG", "onMessageReceived: Title is " + title + "\n hisID" + uidOfSender);
            fetchUser(uidOfSender,title,  message);
        } else Log.d("TAG", "onMessageReceived: no data ");

        super.onMessageReceived(remoteMessage);
    }
    public void updateCurUserToken(@NonNull String newToken){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("which field do you want to update").setValue(newToken);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
    public void fetchUser(String uidOfSender,String title, String message){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(uidOfSender==null){
            return;
        }
        DatabaseReference ref = database.getReference("users/"+uidOfSender);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user =  dataSnapshot.getValue(User.class);
                if(user==null){
                    return;
                }
                user.firstName =dataSnapshot.child("firstName").getValue().toString();
                user.lastName = dataSnapshot.child("lastName").getValue().toString();
                user.email  =  dataSnapshot.child("email").getValue().toString();
                user.city =  dataSnapshot.child("city").getValue().toString();
                user.gender =  dataSnapshot.child("gender").getValue().toString();
                user.uid = dataSnapshot.child("uid").getValue().toString();
                user. url = dataSnapshot.child("url").getValue().toString();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    createOreoNotification( user,title, message);
                else
                    createNormalNotification(user,title, message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }
    private void createNormalNotification(User user, String title, String message) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);
        Intent intent = new Intent(getApplicationContext(), ChatLogActivity.class);
        intent.putExtra(NewMessageActivity.USER_KEY, user);
        getBaseContext().startActivity(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85 - 65), builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(User user, String title, String message) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Intent intent = new Intent(getApplicationContext(), ChatLogActivity.class);
        intent.putExtra(NewMessageActivity.USER_KEY, user);
       // getBaseContext().startActivity(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSmallIcon(R.drawable.ic_cliqueicon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(100, notification);
    }
}
