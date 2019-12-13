package it.thalhammer.warhawkreborn.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

import java.io.IOException;
import java.io.InputStream;

public class ServerDetailFragment extends FragmentBase {
    private static final String ARG_PACKET = "pkt";
    private static final String LOG_TAG = ServerDetailFragment.class.getName();

    private DiscoveryPacket mPacket = null;

    public ServerDetailFragment() {
        // Required empty public constructor
    }

    public static ServerDetailFragment newInstance(DiscoveryPacket pkt) {
        ServerDetailFragment fragment = new ServerDetailFragment();
        Bundle args = new Bundle();
        args.putByteArray(ARG_PACKET, pkt.getBytes());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPacket = new DiscoveryPacket(getArguments().getByteArray(ARG_PACKET));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View v = getView();
        if(v == null) return;
        ((TextView)v.findViewById(R.id.fragment_server_detail_name)).setText(mPacket.getName());
        ((TextView)v.findViewById(R.id.fragment_server_detail_map)).setText(mPacket.getMapName());
        ((TextView)v.findViewById(R.id.fragment_server_detail_mode)).setText(mPacket.getGameMode());
        ((TextView)v.findViewById(R.id.fragment_server_detail_maxplayers)).setText(String.valueOf(mPacket.getMaxPlayers()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_minplayers)).setText(String.valueOf(mPacket.getMinPlayers()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_currentplayers)).setText(String.valueOf(mPacket.getCurrentPlayers()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_timeelapsed)).setText(String.valueOf(mPacket.getTimeElapsed()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_timelimit)).setText(String.valueOf(mPacket.getTimeLimit()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_startwait)).setText(String.valueOf(mPacket.getStartWaitTime()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_spawnwait)).setText(String.valueOf(mPacket.getSpawnWaitTime()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_roundsplayed)).setText(String.valueOf(mPacket.getRoundsPlayed()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_pointlimit)).setText(String.valueOf(mPacket.getPointLimit()));
        ((TextView)v.findViewById(R.id.fragment_server_detail_pointcurrent)).setText(String.valueOf(mPacket.getCurrentPoints()));

        try {
            InputStream ims = getContext().getAssets().open("maps/" + mPacket.getMap() + "/minimap" + mPacket.getMapSize() + ".png");
            ((ImageView)v.findViewById(R.id.fragment_server_detail_image)).setImageDrawable(Drawable.createFromStream(ims, null));
        }
        catch(IOException ex) {
            Log.e(LOG_TAG, "Image for " + mPacket.getMap() + " " + mPacket.getMapSize() + " not found", ex);
            try {
                InputStream ims = getContext().getAssets().open("maps/" + mPacket.getMap() + "/minimap0.png");
                ((ImageView)v.findViewById(R.id.fragment_server_detail_image)).setImageDrawable(Drawable.createFromStream(ims, null));
            }
            catch(IOException e) {
                Log.e(LOG_TAG, "Image for " + mPacket.getMap() + " 0 not found", ex);
            }
        }
    }
}
