package uren.com.myduties.interfaces;


public interface RecyclerViewAdapterCallback {
    void OnRemoved();
    void OnInserted();
    void OnChanged(Object object);
}
