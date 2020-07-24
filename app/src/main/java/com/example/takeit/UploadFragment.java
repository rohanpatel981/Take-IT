package com.example.takeit;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    Button buttonBrowse,buttonAddBook;
    EditText editTextBookName,editTextEdition;
    ImageView im;
    ProgressBar progressBar;
    Spinner spin_book_year, spin_book_sem;

    String bookName, bookEditionYear, bookAcademicYear, bookSem;
    String userID;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    long mili;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef, nDatabaseRef;
    FirebaseFirestore fStore;

    FirebaseAuth fAuth;

    private StorageTask mUploadTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upload,container,false);

        buttonAddBook = v.findViewById(R.id.buttonAddBook);
        buttonBrowse = v.findViewById(R.id.buttonBrowse);

        editTextBookName = v.findViewById(R.id.editTextBookName);
        editTextEdition = v.findViewById(R.id.editTextEdition);
        progressBar = v.findViewById(R.id.progressBar);

        im = v.findViewById(R.id.im);
        spin_book_year = v.findViewById(R.id.spin_book_year);
        spin_book_sem = v.findViewById(R.id.spin_book_sem);


        spin_book_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookAcademicYear = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spin_book_sem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookSem = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mStorageRef = FirebaseStorage.getInstance().getReference("BookDetails");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BookDetails");
        nDatabaseRef = FirebaseDatabase.getInstance().getReference("Uploads");

        fStore = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();

        buttonAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookName = editTextBookName.getText().toString();
                bookEditionYear = editTextEdition.getText().toString();

                if (bookName.isEmpty()){
                    editTextBookName.setError("Book name required");
                    editTextBookName.requestFocus();
                    return;
                }
                if (bookEditionYear.isEmpty()){
                    editTextEdition.setError("Book edition required");
                    editTextEdition.requestFocus();
                    return;
                }
                if (mUploadTask != null && mUploadTask.isInProgress())
                    Toast.makeText(getActivity(),"Upload in progress",Toast.LENGTH_SHORT).show();
                else
                    uploadFile();
            }
        });


        buttonBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        return v;
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void uploadFile(){

        if (mImageUri != null){

            userID = fAuth.getCurrentUser().getUid();
            mili = System.currentTimeMillis();

            final StorageReference fileReference = mStorageRef.child(bookAcademicYear+" "+bookSem).child(userID)
                    .child(mili+"."+getFileExtension(mImageUri));

           mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                        }
                    },500);

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            UploadBookImage upload = new UploadBookImage(bookName,uri.toString(),
                                    bookEditionYear,bookAcademicYear,bookSem);
                           // String uploadID = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(userID).child(String.valueOf(mili)).setValue(upload);

                     /*       DocumentReference documentReference = fStore.collection("BookDetails")
                                    .document(userID);

                            Map<String,Object> imageData = new HashMap<>();
                            imageData.put("imageUrl",uri.toString());
                            //imageData.put("ImageName",mili+"."+getFileExtension(mImageUri));
                            //imageData.put("Duration",java.text.DateFormat.getDateTimeInstance().format(new Date()));
                            imageData.put("name",bookName);
                           // imageData.put("BookAcademicAndSemester",bookAcademicYear+" "+bookSem);
                            imageData.put("sem",bookSem);
                            imageData.put("year",bookAcademicYear);
                            imageData.put("edition",bookEditionYear);

                            Map<String,Object> bookNumber = new HashMap<>();
                            bookNumber.put(String.valueOf(mili),imageData);

                            documentReference.set(bookNumber,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","On success: User Profile is created for "+userID);
                                }

                            }); */
                        }
                    });
                    //...................................................
                    Toast.makeText(getActivity(),"Book Added For Sale",Toast.LENGTH_SHORT).show();

                    editTextBookName.setText(null);
                    editTextEdition.setText(null);
                    im.setImageDrawable(null);

                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        }
        else
            {
            Toast.makeText(getActivity(), "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(im);
        }
    }
}
