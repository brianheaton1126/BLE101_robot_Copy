package com.golftronics.golfball.ble;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = PuttData.class, version = 1)
public abstract class PuttDatabase extends RoomDatabase {

    private static PuttDatabase instance;

    public abstract puttDataDao puttdatadao();

    public static synchronized PuttDatabase getInstance(Context context){

        if (instance == null){

            instance = Room.databaseBuilder(context.getApplicationContext(),
                    PuttDatabase.class,"putt_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void>{

        private puttDataDao puttdatadao;

        private PopulateDbAsyncTask(PuttDatabase db){

           puttdatadao = db.puttdatadao();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            puttdatadao.insert(new PuttData(2221, "Brian",1,
                    2.2,12.0,1,8.5,true,
                    1.5,12.0,0.0,
                    "stopped"));

            puttdatadao.insert(new PuttData(2221, "Brian",2,
                    4.4,10.0,1,8.5,true,
                    1.5,9.0,0.0,
                    "stopped"));

            puttdatadao.insert(new PuttData(2221, "Brian",3,
                    8.8,15.0,1,8.5,false,
                    1.5,17.0,2.0,
                    "stopped"));


            return null;
        }
    }
}
