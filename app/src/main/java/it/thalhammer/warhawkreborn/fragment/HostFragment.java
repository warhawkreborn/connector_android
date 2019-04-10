package it.thalhammer.warhawkreborn.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.SearchServerTask;
import it.thalhammer.warhawkreborn.ServerListListViewAdapter;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

import java.net.Inet4Address;
import java.util.List;

public class HostFragment extends FragmentBase {
    private List<Pair<DiscoveryPacket, Inet4Address>> servers;

    public HostFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    private static class MySearchTask extends SearchServerTask {
        HostFragment parent;
        MySearchTask(HostFragment parent) {
            this.parent = parent;
        }

        @Override
        protected  void onPreExecute()
        {
            final View view = parent.getView();
            if(view == null) return;
            view.findViewById(R.id.fragment_host_pb_search).setVisibility(View.VISIBLE);
            view.findViewById(R.id.fragment_host_no_server_found).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(List<Pair<DiscoveryPacket, Inet4Address>> discoveryPackets) {
            super.onPostExecute(discoveryPackets);
            final View view = parent.getView();
            if(view == null) return;
            updateView(discoveryPackets);
            view.findViewById(R.id.fragment_host_pb_search).setVisibility(View.GONE);
            if(discoveryPackets.isEmpty()) {
                View v = view.findViewById(R.id.fragment_host_no_server_found);
                if(v != null) v.setVisibility(View.VISIBLE);
            }
            if(discoveryPackets.size() == 1 && parent.mListener != null) {
                // Only one server available
                parent.mListener.setFragment(HostServerFragment.newInstance(discoveryPackets.get(0).first, discoveryPackets.get(0).second));
            }
        }

        @Override
        @SafeVarargs
        protected final void onProgressUpdate(List<Pair<DiscoveryPacket, Inet4Address>>... discoveryPacketsArray) {
            super.onProgressUpdate(discoveryPacketsArray);
            if(discoveryPacketsArray == null) return;
            for(List<Pair<DiscoveryPacket, Inet4Address>> discoveryPackets: discoveryPacketsArray) {
                updateView(discoveryPackets);
            }
        }

        private void updateView(List<Pair<DiscoveryPacket, Inet4Address>> discoveryPackets) {
            if(discoveryPackets == null || parent == null) return;
            parent.servers = discoveryPackets;
            final View view = parent.getView();
            if(view == null) return;
            ListView list = view.findViewById(R.id.fragment_host_list);
            if(list == null) return;

            DiscoveryPacket[] array = new DiscoveryPacket[discoveryPackets.size()];
            for(int i=0; i<discoveryPackets.size(); i++) array[i] = discoveryPackets.get(0).first;
            ServerListListViewAdapter adapter = new ServerListListViewAdapter(parent.getActivity(), array);
            list.setAdapter(adapter);
        }
    }
    private MySearchTask task;

    @Override
    public void onViewCreated(@NonNull View v, Bundle saveState) {
        ((Button)v.findViewById(R.id.fragment_host_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = new MySearchTask(HostFragment.this);
                task.execute();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        final View v = getView();
        if(v != null) {
            ListView list = v.findViewById(R.id.fragment_host_list);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position > servers.size()) return;
                    mListener.setFragment(HostServerFragment.newInstance(servers.get(position).first, servers.get(position).second));
                }
            });
        }
        task = new MySearchTask(this);
        task.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }
}
