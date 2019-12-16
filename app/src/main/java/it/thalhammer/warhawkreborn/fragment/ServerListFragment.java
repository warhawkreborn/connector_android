package it.thalhammer.warhawkreborn.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.ServerListListViewAdapter;
import it.thalhammer.warhawkreborn.ServerSearchResponder;
import it.thalhammer.warhawkreborn.model.ServerList;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

public class ServerListFragment extends FragmentBase implements ServerSearchResponder.OnStateChangeListener {

    public ServerListFragment() {
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
        return inflater.inflate(R.layout.fragment_server_list, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle saveState) {
        ServerList slist = ServerSearchResponder.getInstance().getServerList();
        updateList(v, slist);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_server_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.fragment_server_list_menu_action_reload){
            ServerSearchResponder.getInstance().updateServers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateList(View view, ServerList slist) {
        if(view == null || slist == null) return;
        Activity activity = this.getActivity();
        if(activity == null) return;
        int active = 0;
        for(ServerList.Entry e : slist) if(e != null && e.isOnline()) active++;
        final DiscoveryPacket[] packets = new DiscoveryPacket[active];
        for(ServerList.Entry e : slist) {
            if(e == null || !e.isOnline()) continue;
            packets[active-1] = new DiscoveryPacket(e.getResponse());
            active--;
        }
        ServerListListViewAdapter adapter=new ServerListListViewAdapter(activity, packets);
        ListView list = view.findViewById(R.id.fragment_server_list_list);
        if(list == null) return;
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.setFragment(ServerDetailFragment.newInstance(packets[position]));
            }
        });
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
    public void onServerListUpdated(final ServerList slist) {
        final View view = getView();
        if(view == null) return;
        view.post(new Runnable() {
            @Override
            public void run() {
                updateList(view, slist);
            }
        });
    }

    @Override
    public void onServerStart() {

    }

    @Override
    public void onServerStop() {

    }
}
