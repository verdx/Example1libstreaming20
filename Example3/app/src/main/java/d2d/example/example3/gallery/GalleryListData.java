package d2d.example.example3.gallery;

import android.graphics.Bitmap;

public class GalleryListData{

    private String path;
    private Bitmap bitmap;
    private boolean isSelected;

    public GalleryListData(String path, Bitmap thumbnail) {
        this.path = path;
        this.bitmap = thumbnail;
        this.isSelected = false;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}