package com.example.takeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class OnClickDetailsFragment extends Fragment {

    private DatabaseReference mDatabaseRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef;
    private ProgressBar progressBar_Circle;
    //String edition, year, sem, name;
    //String imageUrl;
    //String userName, userBranch, userAcademic;

    //TextView tvImageID;

    ImageView imageViewBook;
    TextView tvBook_Name, tvBook_edition, tvBook_Year, tvBook_Sem, tvUser_Name, tvUser_branch, tvUser_Academic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onclick_details, container, false);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");

        //tvImageID = v.findViewById(R.id.tvImageID);

        tvBook_Name = v.findViewById(R.id.tvBook_Name);
        tvBook_edition = v.findViewById(R.id.tvBook_edition);
        tvBook_Year = v.findViewById(R.id.tvBook_Year);
        //tvBook_Sem = v.findViewById(R.id.tvBook_Sem);

        tvUser_Name = v.findViewById(R.id.tvUser_Name);
        //tvUser_branch = v.findViewById(R.id.tvUser_branch);
        //tvUser_Academic = v.findViewById(R.id.tvUser_Academic);

        imageViewBook = v.findViewById(R.id.imageViewBook);
        progressBar_Circle = v.findViewById(R.id.progressBar_Circle);


        Bundle bundle = new Bundle();
        String imageID = getArguments().getString("ImageID");
        String userID = getArguments().getString("UserID");
        //Toast.makeText(getActivity(),imageID+" "+userID, Toast.LENGTH_SHORT).show();
        //tvImageID.setText(userID+" "+imageID);


            noteRef = db.collection("UserDetails").document(userID);
            noteRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "Error while loading" + e.toString(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot.exists()) {

                        tvUser_Name.setText(documentSnapshot.getString("Name"));
                        //tvUser_branch.setText("Branch: " + documentSnapshot.getString("Branch"));
                        //tvUser_Academic.setText("Class: " + documentSnapshot.getString("AcademicYear"));
                   /* userName = documentSnapshot.getString("Name");
                    userBranch = documentSnapshot.getString("Branch");
                    userAcademic = documentSnapshot.getString("AcademicYear"); */
                    }
                }
            });

            mDatabaseRef.child(imageID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FetchingImageDetailsData data = dataSnapshot.getValue(FetchingImageDetailsData.class);
                    tvBook_Name.setText(data.name);
                    tvBook_edition.setText(data.edition);
                    //tvBook_Sem.setText("Semester: " + data.sem);
                    tvBook_Year.setText(data.year+" "+data.sem);

                    Picasso.get().load(data.imageUrl).placeholder(R.mipmap.ic_launcher).fit().centerCrop().into(imageViewBook, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar_Circle.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar_Circle.setVisibility(View.INVISIBLE);
                        }
                    });


                    // edition = data.edition;
                    // name = data.name;
                    //imageUrl = data.imageUrl;
                    //  sem = data.sem;
                    //  year = data.year;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressBar_Circle.setVisibility(View.INVISIBLE);

                }
            });


        return v;
    }
}
