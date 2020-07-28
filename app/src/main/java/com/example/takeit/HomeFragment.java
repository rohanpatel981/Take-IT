package com.example.takeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ImageAdapter.OnItemClickListener {
    private RecyclerView mRecycleView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef, nDatabaseRef;
    FirebaseStorage mStorage;
    private List<UploadBookImage> mUploads;

    TextView tv_userName;
    private ProgressBar progressBar_Circle;
    private String usersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = db.collection("UserDetails")
            .document(usersID);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home,container,false);



        mRecycleView = v.findViewById(R.id.recycleView);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUploads = new ArrayList<>();

        mAdapter = new ImageAdapter(getActivity(), mUploads);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(HomeFragment.this);

        tv_userName = v.findViewById(R.id.tv_userName);
        progressBar_Circle = v.findViewById(R.id.progressBar_Circle);

        noteRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Toast.makeText(getActivity(),"Error while loading"+e.toString(),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot.exists()){
                    tv_userName.setText("Hello, "+documentSnapshot.getString("Name"));
                }
            }
        });

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");
        nDatabaseRef = FirebaseDatabase.getInstance().getReference("BookMarks");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot r : postSnapshot.getChildren()) {
                        UploadBookImage uploadBookImage = r.getValue(UploadBookImage.class);
                        uploadBookImage.setKey(r.getKey());
                        uploadBookImage.setUserKey(postSnapshot.getKey());
                        mUploads.add(uploadBookImage);
                    }
                }
                mAdapter.notifyDataSetChanged();
                progressBar_Circle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar_Circle.setVisibility(View.INVISIBLE);
             //   Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onItemClick(int position) {

        UploadBookImage selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();
        String selectedUserKey = selectedItem.getUserKey();
        //StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());

        if(!selectedKey.isEmpty()){
            Bundle arg = new Bundle();
            arg.putString("ImageID",selectedKey);
            arg.putString("UserID",selectedUserKey);

            //    HomeFragment homeFragment = new HomeFragment();
            //    homeFragment.setArguments(arg);

            FragmentTransaction t = this.getFragmentManager().beginTransaction();
            Fragment mFrag = new OnClickDetailsFragment();
            t.replace(R.id.fragment_container,mFrag);
            mFrag.setArguments(arg);
            t.addToBackStack(null);
            t.commit();
        }


       // Toast.makeText(getActivity(),"Normal click at position: "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWhatEverClick(int position) {

        UploadBookImage selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();
        String selectedUserKey = selectedItem.getUserKey();

        Bundle arg = new Bundle();
        arg.putString("ImageID",selectedKey);
        arg.putString("UserID",selectedUserKey);

        UploadBookImage uploadBookImage = new UploadBookImage(selectedItem.getName(),selectedItem.getImageUrl(),selectedItem.getEdition(),
                selectedItem.getYear(),selectedItem.getSem());
        nDatabaseRef.child(usersID).child(selectedUserKey).child(selectedKey).setValue(uploadBookImage);

        //BookmarkBookImage bookmarkBookImage = new BookmarkBookImage(selectedUserKey,selectedKey);
       //nDatabaseRef.child(usersID).child(selectedKey).setValue(bookmarkBookImage);



        Toast.makeText(getActivity(),"Saved to Bookmark", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {



        //Toast.makeText(getActivity(),selectedKey+" at: "+position, Toast.LENGTH_SHORT).show();
    }
}
