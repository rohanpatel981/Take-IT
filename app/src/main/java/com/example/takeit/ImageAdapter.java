package com.example.takeit;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<UploadBookImage> mUploads;

    private OnItemClickListener mListener;

    public ImageAdapter(Context context, List<UploadBookImage> uploads){
        mContext = context;
        mUploads = uploads;
    }

    public String getLastItemId(){
        return mUploads.get(mUploads.size()-1).getPriority();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        UploadBookImage uploadCurrent = mUploads.get(position);
        holder.tv_bookName.setText(uploadCurrent.getName());
        holder.tv_year_sem.setText(uploadCurrent.getYear()+" "+uploadCurrent.getSem());
        holder.tv_edition.setText("Edition: "+uploadCurrent.getEdition());
        Picasso.get().load(uploadCurrent.getImageUrl()).placeholder(R.mipmap.ic_launcher).fit().centerCrop().into(holder.image_view_upload);

    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {
        public TextView tv_bookName, tv_year_sem, tv_edition;
        public ImageView image_view_upload;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_bookName = itemView.findViewById(R.id.tv_bookName);
            tv_year_sem = itemView.findViewById(R.id.tv_year_sem);
            tv_edition = itemView.findViewById(R.id.tv_edition);
            image_view_upload = itemView.findViewById(R.id.image_view_upload);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem doWhatever = menu.add(Menu.NONE,1,1,"Bookmark");
            MenuItem delete = menu.add(Menu.NONE,2,2,"View Details");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){

                    switch (item.getItemId()){
                        case 1:
                            mListener.onWhatEverClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;

                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onWhatEverClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
}
