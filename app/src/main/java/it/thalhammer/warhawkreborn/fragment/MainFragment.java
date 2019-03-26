package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.ServerSearchResponder;
import it.thalhammer.warhawkreborn.model.ServerList;

public class MainFragment extends Fragment implements ServerSearchResponder.OnStateChangeListener {

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        updateDisplay();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ServerSearchResponder.getInstance().addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ServerSearchResponder.getInstance().removeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main_menu, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.fragment_main_menu_action_reload){
            ServerSearchResponder.getInstance().updateServers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServerListUpdated(ServerList list) {
        updateDisplay();
    }

    @Override
    public void onServerStart() {
        updateDisplay();
    }

    @Override
    public void onServerStop() {
        updateDisplay();
    }

    private void updateDisplay() {
        final View view = getView();
        if(view == null) return;
        view.post(new Runnable() {
            @Override
            public void run() {
                int status = R.drawable.ic_status_failed;
                if(ServerSearchResponder.getInstance().isActive()) {
                    status = R.drawable.ic_status_ok;
                }
                ((ImageView)view.findViewById(R.id.fragment_main_status_image)).setImageDrawable(ContextCompat.getDrawable(getActivity(), status));
                ((TextView)view.findViewById(R.id.fragment_main_status_text)).setText(ServerSearchResponder.getInstance().getStatusText());
            }
        });
    }
}
