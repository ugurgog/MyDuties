package uren.com.myduties.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uren.com.myduties.R;
import uren.com.myduties.models.TaskType;

public class TaskTypeHelper {

    List<TaskType> types =new ArrayList<>();

    public TaskTypeHelper(Context context) {
        fillTypeMap(context);
    }

    public List<TaskType> getTypes() {
        return types;
    }

    public void setTypes(List<TaskType> types) {
        this.types = types;
    }

    public void fillTypeMap(Context context){
        types.add(new TaskType("home", R.drawable.ic_home_type_24dp, context.getResources().getString(R.string.task_type_home)));
        types.add(new TaskType("shop", R.drawable.ic_shopping_type_24dp, context.getResources().getString(R.string.task_type_shop)));
        types.add(new TaskType("business", R.drawable.ic_business_type_24dp, context.getResources().getString(R.string.task_type_business)));
        types.add(new TaskType("school", R.drawable.ic_school_type_24dp, context.getResources().getString(R.string.task_type_school)));
        types.add(new TaskType("love", R.drawable.ic_love_type_24dp, context.getResources().getString(R.string.task_type_love)));
        types.add(new TaskType("child", R.drawable.ic_child_care_type_24dp, context.getResources().getString(R.string.task_type_child)));
        types.add(new TaskType("pet", R.drawable.ic_pets_type_24dp, context.getResources().getString(R.string.task_type_pet)));
    }
}
