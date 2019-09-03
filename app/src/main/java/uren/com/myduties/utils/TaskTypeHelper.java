package uren.com.myduties.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uren.com.myduties.R;

public class TaskTypeHelper {

    Map<String, Integer> types = new HashMap();

    public TaskTypeHelper() {
        fillTypeMap();
    }

    public Map<String, Integer> getTypes() {
        return types;
    }

    public void setTypes(Map<String, Integer> types) {
        this.types = types;
    }

    public void fillTypeMap(){
        types.put("home", R.drawable.ic_home_type_24dp);
        types.put("shop", R.drawable.ic_shopping_type_24dp);
        types.put("business", R.drawable.ic_business_type_24dp);
        types.put("school", R.drawable.ic_school_type_24dp);
        types.put("love", R.drawable.ic_love_type_24dp);
        types.put("child", R.drawable.ic_child_care_type_24dp);
        types.put("pet", R.drawable.ic_pets_type_24dp);
    }
}
