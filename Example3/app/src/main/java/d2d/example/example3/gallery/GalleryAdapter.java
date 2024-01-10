package d2d.example.example3.gallery;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import d2d.example.example3.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{

    private ArrayList<GalleryListData> listData;
    private final GalleryFragment fragment;
    private Boolean someItemSelected;
    public GalleryAdapter(ArrayList<GalleryListData> listData, GalleryFragment fragment) {
        this.listData = listData;
        this.fragment = fragment;
        this.someItemSelected = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item, parent, false);

        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.textView.setText(listData.get(position).getPath());
        holder.cardView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        if(listData.get(position).getBitmap()!=null){
            holder.shimmer.stopShimmer();
            holder.shimmer.hideShimmer();
            holder.bitmap = listData.get(position).getBitmap();
            holder.imageView.setImageBitmap(holder.bitmap);
        }

        holder.cardView.setOnClickListener(view -> {
            if(someItemSelected){
                selectedItem(holder, position);
            }
            else fragment.startVideo(position);
        });

        holder.imageView.setOnClickListener(view -> {
            if(someItemSelected) {
                selectedItem(holder, position);
            }
            else fragment.startVideo(position);
        });

        holder.cardView.setOnLongClickListener(v -> {
            someItemSelected = true;
            return selectedItem(holder, position);
        });

        holder.imageView.setOnLongClickListener(v -> {
            someItemSelected = true;
            return selectedItem(holder, position);
        });
    }

    public boolean selectedItem(final ViewHolder holder, final int position){
        if(holder.bitmap!=null) {
            if (!listData.get(position).isSelected()) {
                holder.cardView.setBackgroundTintList(ColorStateList.valueOf(fragment.getResources().getColor(R.color.colorGray, null)));
                Drawable d = ResourcesCompat.getDrawable(fragment.getResources(), R.drawable.background_galery_select, null);
                holder.imageView.setImageDrawable(d);
                listData.get(position).setSelected(true);
            } else {
                holder.cardView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                holder.imageView.setImageBitmap(holder.bitmap);
                listData.get(position).setSelected(false);
                if(!isSomeSelected()){
                    someItemSelected = false;
                }
            }
        }
        return true;
    }

    private boolean isSomeSelected(){
        for(GalleryListData ld : listData){
            if(ld.isSelected()) return true;
        }
        return false;
    }

    public void setListData(ArrayList<GalleryListData> listData){
        this.listData = listData;
        this.someItemSelected = false;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;
        private final CardView cardView;
        private final ShimmerFrameLayout shimmer;
        private Bitmap bitmap;

        private ViewHolder(View itemView) {
            super(itemView);
            this.shimmer = itemView.findViewById(R.id.shimmer_view_bitmap);
            shimmer.startShimmer();
            this.imageView = itemView.findViewById(R.id.imageGallery);
            this.textView = itemView.findViewById(R.id.titlegallery);
            this.cardView = itemView.findViewById(R.id.galleryconstraint);
            bitmap = null;
        }
    }
}



