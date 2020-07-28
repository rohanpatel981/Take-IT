package com.example.takeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment implements BookmarkImageAdapter.OnItemClickListener {
    RecyclerView mbRecycleView;
    ProgressBar b_progressBar_Circle;
    List<UploadBookImage> mbUploads;
    BookmarkImageAdapter mbAdapter;

    protected long count;

    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;
    private String usersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bookmarks,container,false);

        mbRecycleView = v.findViewById(R.id.mbrecycleView);
        mbRecycleView.setHasFixedSize(true);
        mbRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mbUploads = new ArrayList<>();
        mbAdapter = new BookmarkImageAdapter(getActivity(), mbUploads);
        mbRecycleView.setAdapter(mbAdapter);
        mbAdapter.setOnItemClickListener(BookmarksFragment.this);

        b_progressBar_Circle = v.findViewById(R.id.b_progressBar_Circle);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookMarks").child(usersID);

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mbUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot r : postSnapshot.getChildren()) {
                        UploadBookImage uploadBookImage = r.getValue(UploadBookImage.class);
                        uploadBookImage.setKey(r.getKey());
                        uploadBookImage.setUserKey(postSnapshot.getKey());
                        mbUploads.add(uploadBookImage);
                    }
                }

                mbAdapter.notifyDataSetChanged();
                b_progressBar_Circle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                b_progressBar_Circle.setVisibility(View.INVISIBLE);
                //   Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        return v;
    }

    @Override
    public void onItemClick(int position) {

        UploadBookImage selectedItem = mbUploads.get(position);
        String selectedKey = selectedItem.getKey();
        String selectedUserKey = selectedItem.getUserKey();

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

        //Toast.makeText(getActivity(),"Normal click at position: "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(getActivity(),"View Details click at position: "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        UploadBookImage selectedItem = mbUploads.get(position);
        final String selectedImageKey = selectedItem.getKey();
        final String selectedUserKey = selectedItem.getUserKey();
        mDatabaseRef.child(selectedUserKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (count!=1) {
            mDatabaseRef.child(selectedUserKey).child(selectedImageKey).removeValue();
        }else {
            mDatabaseRef.child(selectedUserKey).removeValue();
        }
        Toast.makeText(getActivity(),"Bookmark removed", Toast.LENGTH_SHORT).show();

       /* StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedUserKey).removeValue();
                Toast.makeText(getActivity(),"Bookmark removed", Toast.LENGTH_SHORT).show();
            }
        }); */
      //  Toast.makeText(getActivity(),"Delete click at position: "+position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}
