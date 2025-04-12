package com.example.teamnovapersonalprojectprojecting.chat;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamnovapersonalprojectprojecting.R;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageGallery {
    private ConstraintLayout galleryLayout;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;

    private ImageLoader imageLoader;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;

    public Set<Uri> selectedPath;

    private GridLayoutManager layoutManager;
    private Context context;
    private boolean isActive;
    public ImageGallery(Context context, ConstraintLayout galleryLayout) {
        this.galleryLayout = galleryLayout;
        this.context = context;

        recyclerView = galleryLayout.findViewById(R.id.imageGalleryRecyclerView);

        selectedPath = new HashSet<>();

        setActive(false);

        adapter = new ImageAdapter(this);
        recyclerView.setAdapter(adapter);

        layoutManager = new GridLayoutManager(context,3);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        imageLoader = new ImageLoader(context);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if(!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreImages();
                }
            }
        });
        loadMoreImages();
    }

    public void reset(){
        if(adapter != null){
            adapter.clear();
            imageLoader = new ImageLoader(context);
            loadMoreImages();
        }
    }

    public void hideKeyboard(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void setActive(boolean isActive){
        this.isActive = isActive;
        if(isActive){
            hideKeyboard(context, galleryLayout);
            galleryLayout.setVisibility(View.VISIBLE);
            galleryLayout.requestFocus();
        } else {
            galleryLayout.setVisibility(View.GONE);
            reset();
        }
    }
    public void changeActiveState(){
        setActive(!isActive);
    }

    public void loadMoreImages() {
        if(isLoading) return;
        isLoading = true;

        executor.execute(() -> {
            List<Uri> newImages = imageLoader.loadNextPage();
            handler.post(() -> {
                adapter.addImages(newImages);
                isLoading = false;
            });
        });
    }

    public static class ImageLoader {
        private Context context;
        private List<Uri> imageUris;
        private int currentPage = 0;
        private int pageSize = 20;
        public ImageLoader(Context context) {
            this.context = context;
            this.imageUris = getAllImagePaths();
        }

        private List<Uri> getAllImagePaths() {
            List<Uri> imageUri = new ArrayList<>();

            Uri collection;
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

            String[] projection = new String[] {
                    MediaStore.Images.Media._ID
            };

            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

            Log.d("test", collection.toString());
            try (Cursor cursor = context.getContentResolver().query(collection, projection, null, null, sortOrder)) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                while (cursor.moveToNext()){
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    Log.d("test", contentUri.toString());
                    imageUri.add(contentUri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return imageUri;
        }

        public List<Uri> loadNextPage() {
            int start = Math.min(currentPage * pageSize, imageUris.size());
            int end = Math.min((currentPage + 1) * pageSize, imageUris.size());
            ++currentPage;
            return imageUris.subList(start,end);
        }
    }

    public static class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<Uri> imagePaths;
        private ImageGallery imageGallery;

        public ImageAdapter(ImageGallery imageGallery) {
            this.imageGallery = imageGallery;
            imagePaths = new ArrayList<>();
        }
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
            return new ImageViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Uri imageUri = imagePaths.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .into(holder.imageButton);

            holder.imageButton.setOnClickListener((view)->{
                if(holder.checkBox.isChecked()){
                    imageGallery.selectedPath.remove(imageUri);
                    holder.checkBox.setChecked(false);
                } else {
                    imageGallery.selectedPath.add(imageUri);
                    holder.checkBox.setChecked(true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagePaths.size();
        }

        public void addImages(List<Uri> newImages) {
            int startPosition = imagePaths.size();
            imagePaths.addAll(newImages);
            notifyItemRangeInserted(startPosition, newImages.size());
        }

        public void clear(){
            imagePaths.clear();
            this.notifyDataSetChanged();
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageButton imageButton;
        private CheckBox checkBox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
