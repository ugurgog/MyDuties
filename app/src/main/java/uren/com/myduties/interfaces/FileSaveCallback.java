package uren.com.myduties.interfaces;

public interface FileSaveCallback {
    void Saved(String realPath);
    void OnError(Exception e);
}
