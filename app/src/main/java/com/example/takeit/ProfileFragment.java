package com.example.takeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements BookmarkImageAdapter.OnItemClickListener {
    TextView tvName,tvAcademic,tvEmail,tvBookSold,tvBookReceived,tvBookForSale;
    //FirebaseAuth fAuth;
    private String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseStorage mStorage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = db.collection("UserDetails")
            .document(userID);

    RecyclerView recyclerViewProfile;
    DatabaseReference mDatabaseRef,nDatabaseRef;
    private BookmarkImageAdapter imageAdapter;
    private List<UploadBookImage> mUploads;

    int totalBooksForSale=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile,container,false);

        tvName = v.findViewById(R.id.tvName);
        tvAcademic = v.findViewById(R.id.tvAcademic);
      //  tvBranch = v.findViewById(R.id.tvBranch);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvBookSold = v.findViewById(R.id.tvBookSold);
        tvBookReceived = v.findViewById(R.id.tvBookReceived);
        tvBookForSale = v.findViewById(R.id.tvBookForSale);

        recyclerViewProfile = v.findViewById(R.id.recycleViewProfile);
        recyclerViewProfile.setHasFixedSize(true);
        recyclerViewProfile.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        mUploads = new ArrayList<>();
        imageAdapter = new BookmarkImageAdapter(getActivity(),mUploads);
        recyclerViewProfile.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(ProfileFragment.this);

        mStorage = FirebaseStorage.getInstance();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        noteRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Toast.makeText(getActivity(),"Error while loading"+e.toString(),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot.exists()){
                    tvName.setText(documentSnapshot.getString("Name"));
                    tvAcademic.setText(documentSnapshot.getString("AcademicYear")+" "+documentSnapshot.getString("Branch"));
                    tvEmail.setText("Email ID - "+documentSnapshot.getString("EmailID"));
                    tvBookSold.setText("Books Sold: ");
                    tvBookReceived.setText("Books Received: ");
                   // tvBookForSale.setText("Books for sale: ");
                }
            }
        });

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                            UploadBookImage uploadBookImage = postSnapshot.getValue(UploadBookImage.class);
                            if (uploadBookImage.getUserID().equals(userID)){
                                totalBooksForSale++;
                            }
                        }
                        tvBookForSale.setText("Books for sale: "+totalBooksForSale);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        nDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");
        Query query = nDatabaseRef.orderByChild("userID").equalTo(userID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 mUploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    UploadBookImage uploadBookImage = postSnapshot.getValue(UploadBookImage.class);
                    uploadBookImage.setKey(uploadBookImage.getPriority());
                    //uploadBookImage.setUserKey(uploadBookImage.getUserID());
                    mUploads.add(uploadBookImage);
                    //mbUploads.add(uploadBookImage);
                }

                imageAdapter.notifyDataSetChanged();
                //  mbAdapter.notifyDataSetChanged();
                //   b_progressBar_Circle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //  b_progressBar_Circle.setVisibility(View.INVISIBLE);
                //   Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        UploadBookImage selectedItem = mUploads.get(position);

    }

    @Override
    public void onWhatEverClick(int position) {
        UploadBookImage selectedItem = mUploads.get(position);
    }

    @Override
    public void onDeleteClick(int position) {
        final UploadBookImage selectedItem = mUploads.get(position);
        final String selectedImageUniqueKey = selectedItem.getKey();
        String selectedImageUniqueUrl = selectedItem.getImageUrl();

        StorageReference mStorageRef = mStorage.getReferenceFromUrl(selectedImageUniqueUrl);
        mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedImageUniqueKey).removeValue();
                Toast.makeText(getActivity(),selectedItem.getName()+" deleted",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
