package com.example.takeit;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class HomeFragment extends Fragment implements ImageAdapter.OnItemClickListener {
    private RecyclerView mRecycleView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef, nDatabaseRef;
    FirebaseStorage mStorage;
    private List<UploadBookImage> mUploads;

    TextView tv_userName;
    private ProgressBar progressBar_Circle,progressBar_Circle_2;
    private String usersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = db.collection("UserDetails")
            .document(usersID);


    int total_items,current_items,scrollOutItems;
    boolean isScrolling = false, reachedEnd=false;
    LinearLayoutManager manager;

    long totalChildCount;

    Query query;

    String lastChildId;

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (v != null){
            if ((ViewGroup)v.getParent() != null)
                ((ViewGroup)v.getParent()).removeView(v);
            return v;
        }
        v = inflater.inflate(R.layout.fragment_home,container,false);

        progressBar_Circle = v.findViewById(R.id.progressBar_Circle);
        progressBar_Circle_2 = v.findViewById(R.id.progressBar_Circle_2);
        tv_userName = v.findViewById(R.id.tv_userName);
        mRecycleView = v.findViewById(R.id.recycleView);
        manager = new LinearLayoutManager(getActivity());
        //mRecycleView.setHasFixedSize(true);
        //mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUploads = new ArrayList<>();
        mAdapter = new ImageAdapter(getActivity(), mUploads);
        mRecycleView.setAdapter(mAdapter);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");
        nDatabaseRef = FirebaseDatabase.getInstance().getReference("BookMarks");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalChildCount = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRecycleView.setLayoutManager(manager);
        mAdapter.setOnItemClickListener(HomeFragment.this);
        //mRecycleView.setVisibility(View.GONE);
        //fetchData();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query lastQuery = databaseReference.child("BookDetails").orderByChild("priority").limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot r : dataSnapshot.getChildren()){
                    UploadBookImage uploadBookImage = r.getValue(UploadBookImage.class);
                    lastChildId = uploadBookImage.getPriority();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        runQuery(null);
        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                current_items = manager.getChildCount();
                total_items = manager.getItemCount();
                scrollOutItems = manager.findFirstVisibleItemPosition();

                if (isScrolling && (current_items + scrollOutItems == total_items) && !reachedEnd ){
                    isScrolling = false;
                    Toast.makeText(getActivity(),"Fetching Data",Toast.LENGTH_SHORT).show();
                    progressBar_Circle.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runQuery(mAdapter.getLastItemId());
                            progressBar_Circle.setVisibility(View.INVISIBLE);
                        }
                    }, 500);


                }
            }
        });

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


/*
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
*/
        return v;
    }


    public void runQuery(String nodeId){
        if (nodeId == null) {
            query = mDatabaseRef
                    .orderByChild("priority")
                    .limitToFirst(7);

        }
        else{
            query = mDatabaseRef
                    .orderByChild("priority")
                    .startAt(nodeId+1)
                    .limitToFirst(7);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        UploadBookImage uploadBookImage = postSnapshot.getValue(UploadBookImage.class);
                    if (uploadBookImage.getPriority().equals(lastChildId))
                        reachedEnd = true;
                    else
                        reachedEnd = false;

                        uploadBookImage.setKey(uploadBookImage.getPriority());
                        uploadBookImage.setUserKey(uploadBookImage.getUserID());
                        Collections.reverse(mUploads);
                        mUploads.add(uploadBookImage);
                }
                //mRecycleView.setVisibility(View.VISIBLE);
                progressBar_Circle_2.setVisibility(View.INVISIBLE);
                mAdapter.notifyDataSetChanged();
                isScrolling = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                isScrolling = false;
            }
        });
    }



    @Override
    public void onItemClick(int position) {

        UploadBookImage selectedItem = mUploads.get(position);
        //Toast.makeText(getActivity(),String.valueOf(position),Toast.LENGTH_SHORT).show();

        String selectedKey = selectedItem.getKey();
        String selectedUserKey = selectedItem.getUserID();
        //Toast.makeText(getActivity(),selectedKey+" "+selectedUserKey,Toast.LENGTH_SHORT).show();
        //StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());

        if(!selectedKey.isEmpty() && !selectedUserKey.isEmpty()){
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
        else {
            Toast.makeText(getActivity(),"Empty",Toast.LENGTH_SHORT).show();
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

        UploadBookImage uploadBookImage = new UploadBookImage(selectedItem.getName(),
                selectedItem.getImageUrl(),selectedItem.getEdition(), selectedItem.getYear(),
                selectedItem.getSem(), selectedItem.getUserID(),selectedItem.getPriority());
        nDatabaseRef.child(usersID).child(selectedUserKey).child(selectedKey).setValue(uploadBookImage);

        Toast.makeText(getActivity(),"Saved to Bookmark", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {

        //Toast.makeText(getActivity(),selectedKey+" at: "+position, Toast.LENGTH_SHORT).show();
    }


}
