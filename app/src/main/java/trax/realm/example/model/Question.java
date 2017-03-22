package trax.realm.example.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class Question extends RealmObject {
    @SerializedName("title")
    private String title;
    @SerializedName("link")
    private String link;

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
