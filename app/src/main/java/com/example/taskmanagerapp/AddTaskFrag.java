package com.example.taskmanagerapp;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.taskmanagerapp.db.DatabaseClient;
import com.example.taskmanagerapp.db.Task;

public class AddTaskFrag extends Fragment {

    private EditText editTextTask, editTextDesc, editTextFinishBy;
    private AddTaskViewModel mViewModel;

    public static AddTaskFrag newInstance() {
        return new AddTaskFrag();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.add_task_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AddTaskViewModel.class);

        editTextTask = getView().findViewById(R.id.editTextTask);
        editTextDesc = getView().findViewById(R.id.editTextDesc);
        editTextFinishBy = getView().findViewById(R.id.editTextFinishBy);

        getView().findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
    }

    private void saveTask() {
        final String sTask = editTextTask.getText().toString().trim();
        final String sDesc = editTextDesc.getText().toString().trim();
        final String sFinishBy = editTextFinishBy.getText().toString().trim();

        if (sTask.isEmpty()){
            editTextTask.setError("Task Required");
            editTextTask.requestFocus();
            return;
        }

        if (sDesc.isEmpty()){
            editTextDesc.setError("Desc required");
            editTextDesc.requestFocus();
            return;
        }

        if (sFinishBy.isEmpty()){
            editTextFinishBy.setError("Finish by required");
            editTextFinishBy.requestFocus();
        }

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids){
                Task task = new Task();
                task.setTask_name(sTask);
                task.setDesc(sDesc);
                task.setFinishBy(sFinishBy);
                task.setFinished(false);

                DatabaseClient.getInstance(getActivity().getApplicationContext())
                        .getAppDatabase()
                        .taskDao()
                        .insert(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                AddTaskFrag fragment = (AddTaskFrag) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment != null){
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.remove(fragment).commit();
                    new MainActivity().setFragmentDisplayedFalse();
                    Intent intent = new Intent(getContext(),MainActivity.class);
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0,0);
                }
                Toast.makeText(getActivity().getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    public AddTaskFrag (){

    }

}