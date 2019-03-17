package it.thalhammer.warhawkreborn;

import android.os.AsyncTask;

public class HostingHelper extends AsyncTask<Void, String, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress("Hello");
        return null;
    }
}
