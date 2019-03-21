package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ListView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.ServerListListViewAdapter;
import it.thalhammer.warhawkreborn.ServerSearchResponder;
import it.thalhammer.warhawkreborn.model.ServerList;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

public class ServerListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public ServerListFragment() {
    }

    public static ServerListFragment newInstance(String param1, String param2) {
        ServerListFragment fragment = new ServerListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server_list, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle saveState) {
        ServerList slist = ServerSearchResponder.getInstance().getServerList();
        int active = 0;
        for(ServerList.Entry e : slist) if(e.isOnline()) active++;
        DiscoveryPacket[] packets = new DiscoveryPacket[active];
        for(int i=0; i<slist.size(); i++) {
            if(!slist.get(i).isOnline()) continue;
            packets[active-1] = new DiscoveryPacket(slist.get(i).getResponse());
            active--;
        }
        ServerListListViewAdapter adapter=new ServerListListViewAdapter(this.getActivity(), packets);
        ListView list =(ListView)getView().findViewById(R.id.fragment_server_list_list);
        list.setAdapter(adapter);
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
