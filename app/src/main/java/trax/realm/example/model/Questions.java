package trax.realm.example.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Questions {
    @SerializedName("items")
    List<Question> items;

    public List<Question> getItems() {
        return items;
    }

    public void setItems(List<Question> items) {
        this.items = items;
    }
}
