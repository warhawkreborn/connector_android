package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.ServerListListViewAdapter;
import it.thalhammer.warhawkreborn.ServerSearchResponder;
import it.thalhammer.warhawkreborn.model.ServerList;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

public class ServerListFragment extends FragmentBase {

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
    public void onViewCreated(@NonNull View v, Bundle saveState) {
        ServerList slist = ServerSearchResponder.getInstance().getServerList();
        int active = 0;
        for(ServerList.Entry e : slist) if(e.isOnline()) active++;
        final DiscoveryPacket[] packets = new DiscoveryPacket[active];
        for(ServerList.Entry e : slist) {
            if(!e.isOnline()) continue;
            packets[active-1] = new DiscoveryPacket(e.getResponse());
            active--;
        }
        ServerListListViewAdapter adapter=new ServerListListViewAdapter(this.getActivity(), packets);
        ListView list = v.findViewById(R.id.fragment_server_list_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.setFragment(ServerDetailFragment.newInstance(packets[position]));
            }
        });
    }
}
