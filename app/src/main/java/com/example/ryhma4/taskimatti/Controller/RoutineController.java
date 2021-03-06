package com.example.ryhma4.taskimatti.Controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ryhma4.taskimatti.R;
import com.example.ryhma4.taskimatti.activity.MainActivity;
import com.example.ryhma4.taskimatti.model.Routine;
import com.example.ryhma4.taskimatti.model.Task;
import com.example.ryhma4.taskimatti.model.Type;
import com.example.ryhma4.taskimatti.model.TypeColor;
import com.example.ryhma4.taskimatti.utility.CallbackHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anttu on 26.2.2018.
 */

public class RoutineController implements CallbackHandler {
    private static RoutineController instance;
    private Database db;
    private ArrayList<Routine> routines;
    private ArrayList<Type> types, allTypes;
    private ArrayList<ArrayList<Routine>> routinesByType;
    private HashMap<Type, ArrayList<Routine>> routinesByHeader;

    private RoutineController() {
        db = Database.getInstance();
        routines = new ArrayList<>();
        types = new ArrayList<>();
        routinesByType = new ArrayList<>();
        routinesByType.add(new ArrayList<Routine>());
        routinesByHeader = new HashMap<>();
        allTypes = new ArrayList<>();

        fetchRoutines();
    }

    public static RoutineController getInstance() {
        if (instance == null) {
            synchronized (RoutineController.class) {
                if (instance == null) {
                    instance = new RoutineController();
                }
            }
        }
        return instance;
    }

    /**
     * Fetches routines from the database, creates a new Type object with the name of "All" if not already present.
     */
    public void fetchRoutines() {
        if (findTypeIndex(MainActivity.globalRes.getString(R.string.text_all)) < 0) {
            Type allType = new Type();
            allType.setColor("#ffffff");
            allType.setName(MainActivity.globalRes.getString(R.string.text_all));
            allType.setTypeId("0");
            types.add(allType);
        }
        db.getUserRoutines(this);
    }

    /**
     * Removes the given routine and checks to see if the routines type should also be removed.
     * @param routine
     */
    public void removeRoutine(Routine routine) {
        int routineIndex = findRoutineIndex(routine);
        int typeIndex = findTypeIndex(routine.getType().getName());
        if (routinesByType.get(typeIndex).size() <= 1) {
            types.remove(typeIndex);
        }
        routines.remove(routineIndex);
        TaskController.getInstance().updateTasks();
        getRoutinesByHeader();
    }

    /**
     * Updates the routines information on screen and in the database.
     * @param routine Routine object to be updated
     */
    public void updateRoutine(Routine routine) {
        int routineIndex = findRoutineIndex(routine);
        int typeIndex = findTypeIndex(routine.getType().getName());
        int prevTypeIndex = findTypeIndex(routines.get(routineIndex).getType().getName());

        //Check if the the type of the routine has been changed and remove the type from the list if no other routine uses the same type.
        if(typeIndex != prevTypeIndex) {
            if(routinesByType.get(prevTypeIndex).size() <= 1) {
                types.remove(prevTypeIndex);
            }
            if(typeIndex < 0) {
                routine.getType().setColor(TypeColor.randomColor());
                types.add(routine.getType());
            }
        }
        else {
            routine.getType().setColor(types.get(typeIndex).getColor());
        }

        routines.set(routineIndex, routine);
        db.updateRoutine(routine);
        getRoutinesByHeader();
    }

    /**
     * Clears and updates the routines and types lists.
     */
    public void updateRoutinesAndTypes() {
        routines.clear();
        types.clear();
        fetchRoutines();
    }

    public ArrayList<Routine> getRoutines() {
        return routines;
    }

    /**
     * Clear and then generates the routinesByType ArrayList, which holds all of the users routines arranged by type.
     */
    public void setRoutinesByType() {
        routinesByType.clear();
        routinesByType.add(new ArrayList<Routine>());

        for (Routine routine : routines) {
            int index = findTypeIndex(routine.getType().getName());

            if (index > routinesByType.size() - 1) {
                routinesByType.add(new ArrayList<Routine>());
            }

            routinesByType.get(0).add(routine);
            routinesByType.get(index).add(routine);
        }
    }

    public ArrayList<ArrayList<Routine>> getRoutinesByType() {
        return routinesByType;
    }

    /**
     * Clears and then generates the routinesByHeader HashMap
     * @return HashMap with a key of Type object and value as an ArrayList of corresponding Routine objects.
     */
    public HashMap<Type, ArrayList<Routine>> getRoutinesByHeader() {
        routinesByHeader.clear();
        setRoutinesByType();
        for (int i = 0; i < types.size(); i++) {
            routinesByHeader.put(types.get(i), routinesByType.get(i));
        }
        return routinesByHeader;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }

    public void setRoutine(Routine routine) {
        db.setRoutine(routine);
    }

    /**
     * Finds the index of the type from the types list
     *
     * @param name Name of the type
     * @return Returns the index of the type or -1 if not found
     */
    public int findTypeIndex(String name) {
        int index = -1;
        for (int i = 0; i < types.size(); i++) {
            if (name.equals(types.get(i).getName())) {
                index = i;
            }
        }
        return index;
    }

    /**
     * Get the Type object of a routine by it's ID.
     * @param id Id of the task's type
     * @return Type object
     */
    public Type getTypeById(String id) {
        Type type = null;
        for(Type item : allTypes) {
            if (item.getTypeId().equals(id)) {
                type = item;
            }
        }
        /*
        Current implementation causes Type objects to share a name, but not ID or color.
        To make sure that the correct Type object is returned, get the Type by name from the unique Types list.
         */
        type = types.get(findTypeIndex(type.getName()));
        return type;
    }

    /**
     * Finds the index of the routine from the routines list
     *
     * @param routine the object to check
     * @return Returns the index of the routine or -1 if not found
     */
    public int findRoutineIndex(Routine routine) {
        int index = -1;
        for (int i = 0; i < routines.size(); i++) {
            if (routine.getRoutineId().equals(routines.get(i).getRoutineId())) {
                index = i;
            }
        }
        return index;
    }

    @Override
    public void successHandler(ArrayList<?> list) {
    }

    @Override
    public void errorHandler() {

    }

    @Override
    public void passObject(Object object) {
        Routine routine = (Routine) object;
        if (findRoutineIndex(routine) < 0) {
            routines.add(routine);
        }

        Type type = routine.getType();
        allTypes.add(type);
        if (findTypeIndex(type.getName()) < 0) {
            types.add(type);
        }
    }

    public void setTask(ArrayList<Task> tasks) {
        db.setTask(tasks);
        TaskController.getInstance().fetchActiveTasks();
    }

    /**
     * OntouchListener for routine's description field. If the description is longer than the field, you can scroll through it.
     *
     * @param descriptionView The description's TextView element
     * @param context         The current view's context
     */
    public void routineDescriptionTouchListener(final TextView descriptionView, final Context context) {
        descriptionView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_BUTTON_PRESS:
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(descriptionView, InputMethodManager.SHOW_IMPLICIT);
                }
                return false;
            }
        });
    }

}
