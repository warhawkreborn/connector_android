package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Consumer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.Util;
import it.thalhammer.warhawkreborn.model.UpdateInfo;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private UpdateInfo info;

    /*private class UpdateTask extends AsyncTask<UpdateInfo, Double, Void> {
        @Override
        protected Void doInBackground(UpdateInfo... infos) {
            try
            {
                URLConnection con = new URL("https://warhawk.thalhammer.it/" + infos[0].getFilename()).openConnection();
                final int len = con.getContentLength();
                InputStream is = con.getInputStream();

                File targetFile = new File(UpdateFragment.this.getContext().getFilesDir(), "update.apk");
                OutputStream os = new FileOutputStream(targetFile);
                Util.copyStream(is, os, new Consumer<Integer>() {
                    @Override
                    public void accept(Integer done) {
                        publishProgress(len == -1?0.0:(((double)done)/len));
                    }
                });

                Uri fileUri = Uri.fromFile(targetFile);
                Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                UpdateFragment.this.getContext().startActivity(intent);
                UpdateFragment.this.getActivity().finish();
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            } finally {
            }
            return null;
        }
    }*/

    public UpdateFragment() {
    }

    public static UpdateFragment newInstance(UpdateInfo info) {
        UpdateFragment fragment = new UpdateFragment();
        Bundle args = new Bundle();
        args.putBundle("info", info.toBundle());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            info = UpdateInfo.fromBundle(getArguments().getBundle("info"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle saveState) {
        ((TextView)getView().findViewById(R.id.fragment_update_changes)).setText(info.getChanges());
        ((Button)getView().findViewById(R.id.fragment_update_btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.setDrawerVisible(true);
                mListener.setFragment(new MainFragment());
            }
        });
        ((Button)getView().findViewById(R.id.fragment_update_btn_update)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getView().findViewById(R.id.fragment_update_section_info).setVisibility(View.GONE);
                //getView().findViewById(R.id.fragment_update_section_progress).setVisibility(View.VISIBLE);

                //UpdateTask task = new UpdateTask();
                //task.execute(info);
                String url = "https://warhawk.thalhammer.it/" + info.getFilename();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        mListener.setDrawerVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void setFragment(Fragment f);
        void setDrawerVisible(boolean v);
    }
}
