package com.golftronics.golfball.ble;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PuttRepository {

    private puttDataDao puttDao;
    private LiveData<List<PuttData>> allPutts;
    private LiveData<Integer> allLongPutts;


    public PuttRepository(Application application){

        PuttDatabase database = PuttDatabase.getInstance(application);
        puttDao = database.puttdatadao();
        //allPutts = puttDao.getAllPutts();
        allLongPutts = puttDao.getAllLongPutts();
    }

    public void insert(PuttData puttsegment){

        new InsertPuttAsyncTask(puttDao).execute(puttsegment);

    }

    public void update(PuttData puttsegment){

        new UpdatePuttAsyncTask(puttDao).execute(puttsegment);

    }

    public void delete(PuttData puttsegment){

        new DeletePuttAsyncTask(puttDao).execute(puttsegment);

    }

    public void deleteAllPutts(PuttData puttsegment){

        new DeleteAllPuttAsyncTask(puttDao).execute();


    }



    public LiveData<List<PuttData>> getAllPutts(){
        return allPutts;
    }

    public LiveData<Integer> getAllLongPutts() {


        return puttDao.getAllLongPutts();}


    private static class InsertPuttAsyncTask extends AsyncTask<PuttData,Void,Void>{

        private puttDataDao puttDao;

        private InsertPuttAsyncTask(puttDataDao puttDao){

            this.puttDao = puttDao;
        }

        @Override
        protected Void doInBackground(PuttData... puttData) {

            puttDao.insert(puttData[0]);

            return null;

        }
    }

    private static class UpdatePuttAsyncTask extends AsyncTask<PuttData,Void,Void>{

        private puttDataDao puttDao;

        private UpdatePuttAsyncTask(puttDataDao puttDao){

            this.puttDao = puttDao;
        }

        @Override
        protected Void doInBackground(PuttData... puttData) {

            puttDao.update(puttData[0]);

            return null;

        }
    }

    private static class DeletePuttAsyncTask extends AsyncTask<PuttData,Void,Void>{

        private puttDataDao puttDao;

        private DeletePuttAsyncTask(puttDataDao puttDao){

            this.puttDao = puttDao;
        }

        @Override
        protected Void doInBackground(PuttData... puttData) {

            puttDao.delete(puttData[0]);

            return null;

        }
    }


    private static class DeleteAllPuttAsyncTask extends AsyncTask<Void,Void,Void>{

        private puttDataDao puttDao;

        private DeleteAllPuttAsyncTask(puttDataDao puttDao){

            this.puttDao = puttDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            puttDao.deleteAllPutts();

            return null;

        }
    }




}
