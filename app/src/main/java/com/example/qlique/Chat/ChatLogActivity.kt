package com.example.qlique
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.qlique.Profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import org.json.JSONException
import org.json.JSONObject

class ChatLogActivity : AppCompatActivity() {
    var NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send"
    var SERVER_KEY = "AAAAxFzL4DI:APA91bF0XL_FWScS0pjrd3xbEpiYQ4tYn99_gHLXWDkxti202bXk9KMxPyuUUGrcHdIWQf9zCdm3Wmmk0_CKKOWWfpOrWQFffEguUytD0mW-U2c5CaTE3pGcIkocOlzGzPGXt9skLgzt"
    val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
    /**
     * Returns User object from the Snapshot.
     * @param snapshot - DataSnapshot
     * @return User object
     */
    private fun loadUser(snapshot: DataSnapshot):User{
        val user  = User()
        user.firstName = snapshot.child("firstName").value.toString()
        user.lastName = snapshot.child("lastName").value.toString()
        user.email  =  snapshot.child("email").value.toString()
        user.city =  snapshot.child("city").value.toString()
        user.gender =  snapshot.child("gender").value.toString()
        user.uid = snapshot.child("uid").value.toString()
        user. url = snapshot.child("url").value.toString()
        return user;
    }
    @SuppressLint("SetTextI18n")
    /**
     * Sets the recyclerView , ActionBar , user's data , sending anf recieving logic.
     * @param savedInstanceState- Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerView_chat.adapter = adapter
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title= user?.firstName+ " "+user?.lastName
        other_side_user.text = user?.firstName+ " "+user?.lastName
        Picasso.get().load(user?.url).into(other_side_photo)
        sendBtn.setOnClickListener{
            performSendMessage()
        }
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val curUser: User? = loadUser(snapshot)
                if (curUser != null) {
                    Log.d(ContentValues.TAG, "listening")
                    listenForMessages(curUser)
                }
            }

            override fun onCancelled(po: DatabaseError) {
            }
        })
    }
    /**
     * Return from activity.
     */
    override fun onBackPressed() {
        finish()
    }
    /**
     * Class which represents message from chat.
     */
    class ChatMessage(
        val id: String, val text: String,
        val fromId: String, val toId: String, val timeStamp: Long
    ){
        constructor(): this(
            "", "", "",
            "", -1
        )

    }
    /**
     * Sends the notification to source after performing send.
     * @param message - the message's content to show, targetUid: the uid of the dest.
     */
    private fun handleNotificationToSrc(message: String, targetUid: String){
        val database = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val userFullName =
                    snapshot.child("firstName").value.toString() + "  " + snapshot.child(
                        "lastName"
                    ).value.toString()
                if (user != null) {
                    sendNotificationToSrc(message, targetUid, userFullName, user)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    /**
     * Sends the notification to source after performing send.
     * @param message: message content,
     * @param targetUid: Destination uid,
     * @param userFullName: sender's full name
     * @param senderUser: User
     */
    private fun sendNotificationToSrc(
        message: String,
        targetUid: String,
        userFullName: String,
        senderUser: User
    ) {
        val database = FirebaseDatabase.getInstance().getReference("users").child(targetUid)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val token = user?.tokenFCM ?: return
                val to = JSONObject()
                val data = JSONObject()
                try {
                    data.put("title", userFullName)
                    data.put("message", message)
                    data.put("SenderUid", senderUser.uid)
                    to.put("to", token)
                    to.put("data", data)
                    // Send notification
                    sendNotification(to)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    /**
     * Sends the notification to source after performing send.
     */
    private fun performSendMessage(){
        // Message content
        val textInput = text_message_chat.text.toString()
        val dividerFactor = 1000
        if(textInput.isEmpty()){
            return
        }
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId =  FirebaseAuth.getInstance().uid
        val toId = user.uid
        if (fromId==null)return
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef=FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMsg = ChatMessage(
            ref.key!!,
            textInput,
            fromId,
            toId,
            System.currentTimeMillis() / dividerFactor
        )
        ref.setValue(chatMsg).addOnSuccessListener {
            text_message_chat.text.clear()
            recyclerView_chat.scrollToPosition(adapter.itemCount - 1) }
        toRef.setValue(chatMsg)
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMsg)
        val latestMessageRefTo = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRefTo.setValue(chatMsg)
        handleNotificationToSrc(textInput, toId)
    }
    /**
     * Sends the by the json object.
     * @param  messageBody - JSONObject
     */
    private fun sendNotification(messageBody: JSONObject) {
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, NOTIFICATION_URL, messageBody,
            Response.Listener { response: JSONObject ->
                Log.d(
                    "notification",
                    "sendNotification: $response"
                )
            },
            Response.ErrorListener { error: VolleyError ->
                Log.d(
                    "notification",
                    "sendNotification: $error"
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map: MutableMap<String, String> = HashMap()
                map["Authorization"] = "key=$SERVER_KEY"
                map["Content-Type"] = "application/json"
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(request)
    }
    /**
     * Inserts received messages to the adapter.
     * @param  curUser - User
     */
    fun listenForMessages(curUser: User){
        val fromId= FirebaseAuth.getInstance().uid
        val toId =intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java)
                // Gets user's uid
                val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                if (msg != null) {
                    if (msg.fromId == FirebaseAuth.getInstance().uid && msg.toId == toUser!!.uid) {
                        adapter.add(ChatFromItem(msg.text, curUser!!))
                    } else if (msg.fromId == toUser!!.uid && msg.toId == FirebaseAuth.getInstance().uid) {
                        //adds message to adapter
                        adapter.add(ChatToItem(msg.text, toUser!!))
                    }
                    // scroll the recycler view -1 position
                    recyclerView_chat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }
}
/**
 * Class for messages that user received
 * @param text - message's content
 * @param  user: User
 */
class ChatFromItem(val text: String, val user: User): Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.messageFrom.text= text
        //load user image
        val uri = user.url
        val targetImageView = viewHolder.itemView.circularImageViewFrom
        if (uri != null && uri != "") {
            Picasso.get().load(uri).into(targetImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
/**
 * Class for messages that user sent
 * @param text - message's content
 * @param  user: User
 */
class ChatToItem(val string: String, val user: User): Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.messageTo.text = string
        val uri = user.url
        val targetImageView = viewHolder.itemView.circularImageViewTo
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}