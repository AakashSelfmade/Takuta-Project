package com.example.takuta.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId() != null){
            return true;
        }else{
            return false;
        }
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionRefrence(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatRoomRefrence(String chatRoomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatRoomId);
    }

    public static CollectionReference getChatRoomMessageRefrence(String chatRoomId){
        return getChatRoomRefrence(chatRoomId).collection("chats");
    }

    public static String getChatRoomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static CollectionReference allChatroomCollectionRefrence(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String>userId){
        if (userId.get(0).equals(FirebaseUtil.currentUserId())){
            return allUserCollectionRefrence().document(userId.get(1));
        }else{
            return allUserCollectionRefrence().document(userId.get(0));
        }
    }

    public static String timestamptoString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference getCurrentProfilePicStorage(){
        return FirebaseStorage.getInstance().getReference().child("Profile_Pic")
                .child(FirebaseUtil.currentUserId());
    }

    public static StorageReference getOtherProfilePicStorage(String otherUsedId){
        return FirebaseStorage.getInstance().getReference().child("Profile_Pic")
                .child(otherUsedId);
    }
}
