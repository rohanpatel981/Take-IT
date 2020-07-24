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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ProfileFragment extends Fragment {
    TextView tvName,tvAcademic,tvBranch;
    //FirebaseAuth fAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = db.collection("UserDetails")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile,container,false);

        tvName = v.findViewById(R.id.tvName);
        tvAcademic = v.findViewById(R.id.tvAcademic);
        tvBranch = v.findViewById(R.id.tvBranch);

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
                    tvAcademic.setText(documentSnapshot.getString("AcademicYear"));
                    tvBranch.setText(documentSnapshot.getString("Branch"));
                }
            }
        });
    }
}
