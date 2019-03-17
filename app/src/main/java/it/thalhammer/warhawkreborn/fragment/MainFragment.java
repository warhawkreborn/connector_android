package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import it.thalhammer.warhawkreborn.AppLog;
import it.thalhammer.warhawkreborn.MainActivity;
import it.thalhammer.warhawkreborn.R;

import java.util.List;

public class MainFragment extends Fragment implements AppLog.OnLogListener {
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((TextView)view.findViewById(R.id.log_view)).setMovementMethod(new ScrollingMovementMethod());
        this.onLogUpdated(AppLog.getInstance().getEntries());
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
        AppLog.getInstance().addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        AppLog.getInstance().removeListener(this);
    }

    @Override
    public void onLogUpdated(List<String> entries) {
        String text = "";
        for(String e : entries) {
            text += e + "\n";
        }

        final String lstr = text;
        getView().post(new Runnable() {
            @Override
            public void run() {
                ((TextView)getView().findViewById(R.id.log_view)).setText(lstr);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
