package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import it.thalhammer.warhawkreborn.MainActivity;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.SearchServerTask;
import it.thalhammer.warhawkreborn.ServerListListViewAdapter;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

import java.net.Inet4Address;
import java.util.List;

public class HostFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private List<Pair<DiscoveryPacket, Inet4Address>> servers;

    public HostFragment() {
        // Required empty public constructor
    }

    public static HostFragment newInstance(String param1, String param2) {
        HostFragment fragment = new HostFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle saveState) {
        SearchServerTask task = new SearchServerTask() {
            @Override
            protected void onPostExecute(List<Pair<DiscoveryPacket, Inet4Address>> discoveryPackets) {
                super.onPostExecute(discoveryPackets);
                DiscoveryPacket[] array = new DiscoveryPacket[discoveryPackets.size()];
                for(int i=0; i<discoveryPackets.size(); i++) array[i] = discoveryPackets.get(0).first;
                ServerListListViewAdapter adapter=new ServerListListViewAdapter(getActivity(), array);
                ListView list =(ListView)getView().findViewById(R.id.fragment_host_list);
                list.setAdapter(adapter);
                getView().findViewById(R.id.fragment_host_pb_search).setVisibility(View.GONE);
                if(discoveryPackets.isEmpty())
                    getView().findViewById(R.id.fragment_host_no_server_found).setVisibility(View.VISIBLE);
                if(discoveryPackets.size() == 1) {
                    // Only one server available
                    ((MainActivity)getActivity()).setFragment(HostServerFragment.newInstance(discoveryPackets.get(0).first, discoveryPackets.get(0).second));
                }
                servers = discoveryPackets;
            }

            @Override
            protected void onProgressUpdate(List<Pair<DiscoveryPacket, Inet4Address>>... discoveryPackets) {
                super.onProgressUpdate(discoveryPackets);
                if(discoveryPackets.length < 1) return;
                DiscoveryPacket[] array = new DiscoveryPacket[discoveryPackets[0].size()];
                for(int i=0; i<discoveryPackets[0].size(); i++) array[i] = discoveryPackets[0].get(0).first;
                ServerListListViewAdapter adapter=new ServerListListViewAdapter(getActivity(), array);
                ListView list =(ListView)getView().findViewById(R.id.fragment_host_list);
                list.setAdapter(adapter);
                servers = discoveryPackets[0];
            }
        };
        task.execute();

        ListView list =(ListView)getView().findViewById(R.id.fragment_host_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position > servers.size()) return;
                ((MainActivity)getActivity()).setFragment(HostServerFragment.newInstance(servers.get(position).first, servers.get(position).second));
            }
        });
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
    }
}
