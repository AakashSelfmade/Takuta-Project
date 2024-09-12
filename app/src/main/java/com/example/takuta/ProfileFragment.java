package com.example.takuta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CpuUsageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.takuta.model.UserModel;
import com.example.takuta.utils.AndroidUtil;
import com.example.takuta.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    Button updateProfileButton;
    ProgressBar progressBar;
    TextView logoutButton;
    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePicLancher;
    Uri selectedImageUri;

    public  ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicLancher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == ChatActivity.RESULT_OK){
                        Intent data = result.getData();
                        if (data!=null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            AndroidUtil.setProfilePic(getContext(),selectedImageUri,profilePic);
                        }
                    }
                }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profile_imageview);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phoneNumber);
        updateProfileButton = view.findViewById(R.id.profile_updateButton);
        progressBar = view.findViewById(R.id.profile_progressbar);
        logoutButton = view.findViewById(R.id.logout_button);

        getUserData();

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButtonClick();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FirebaseUtil.logout();
                            Intent intent = new Intent(getContext(), splashActiviy.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });

            }
        });

        profilePic.setOnClickListener((v)->{
                ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePicLancher.launch(intent);
                                return null;
                            }
                        });
        });

        return view;
    }

    void updateButtonClick(){
        String newUsername = usernameInput.getText().toString();
        if(newUsername.isEmpty()||newUsername.length()<3){
            usernameInput.setError("Username length should be greater than 3 character");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);
        if (selectedImageUri!=null) {
            FirebaseUtil.getCurrentProfilePicStorage().putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        updateToFirestore();
                    });
        }else {
            updateToFirestore();
        }

    }

    void updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setInProgress(false);
                        if (task.isSuccessful()){
                            AndroidUtil.showToast(getContext(),"Updated Successfully");
                        }else {
                            AndroidUtil.showToast(getContext(),"Updated Failed");
                        }
                    }
                });
    }

    void getUserData(){
        setInProgress(true);
        FirebaseUtil.getCurrentProfilePicStorage().getDownloadUrl()
                        .addOnCompleteListener(task -> {
                           if (task.isSuccessful()){
                               Uri uri = task.getResult();
                               AndroidUtil.setProfilePic(getContext(),uri,profilePic);
                           }
                        });
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInProgress(false);
                currentUserModel = task.getResult().toObject(UserModel.class);
                usernameInput.setText(currentUserModel.getUsername());
                phoneInput.setText(currentUserModel.getPhone());
            }
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfileButton.setVisibility(View.VISIBLE);
        }

    }
}