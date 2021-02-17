package com.golftronics.golfball.ble;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PuttViewModel extends AndroidViewModel {

    private PuttRepository repository;
    private LiveData<List<PuttData>> allPutts;
    private LiveData<Integer> allLongPutts;


    public PuttViewModel(@NonNull Application application) {
        super(application);

        repository = new PuttRepository(application);
        allPutts = repository.getAllPutts();
        allLongPutts = repository.getAllLongPutts();
    }

    public void insert(PuttData puttsegment){

        repository.insert(puttsegment);

    }

    public void update(PuttData puttsegment){

        repository.update(puttsegment);

    }

    public void delete(PuttData puttsegment){

        repository.delete(puttsegment);

    }

    public void deleteAllPutts(){


    }




    public LiveData<List<PuttData>> getAllPutts(){
        return allPutts;
    }

    public LiveData<Integer> getAllLongPutts(){
        return repository.getAllLongPutts();
    }



}
