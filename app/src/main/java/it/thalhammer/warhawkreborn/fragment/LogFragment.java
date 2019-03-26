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
import it.thalhammer.warhawkreborn.R;

import java.util.List;

public class LogFragment extends FragmentBase implements AppLog.OnLogListener {

    public LogFragment() {
        // Required empty public constructor
    }

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
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
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((TextView)view.findViewById(R.id.log_view)).setMovementMethod(new ScrollingMovementMethod());
        this.onLogUpdated(AppLog.getInstance().getEntries());
    }

    @Override
    public void onLogUpdated(final List<String> entries) {
        View v = getView();
        if(v != null) v.post(new Runnable() {
            @Override
            public void run() {
                String text = "";
                for(String e : entries) {
                    text += e + "\n";
                }
                View view = getView();
                if(view != null)
                    ((TextView)view.findViewById(R.id.log_view)).setText(text);
            }
        });
    }
}
