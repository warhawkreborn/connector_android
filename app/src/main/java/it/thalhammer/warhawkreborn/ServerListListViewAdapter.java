package it.thalhammer.warhawkreborn;

import android.app.Activity;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

import java.io.IOException;
import java.io.InputStream;


public class ServerListListViewAdapter extends ArrayAdapter<DiscoveryPacket> {

    private static final String LOG_TAG = ServerListListViewAdapter.class.getName();

    private final Activity context;
    private final DiscoveryPacket[] packets;

    public ServerListListViewAdapter(Activity context, DiscoveryPacket[] packets) {
        super(context, R.layout.serverlist_item, packets);

        this.context = context;
        this.packets = packets;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.serverlist_item, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.serverlist_item_title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.serverlist_item_icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.serverlist_item_subtitle);

        titleText.setText(packets[position].getName());
        subtitleText.setText(packets[position].getGameMode() + " " + packets[position].getMapName());

        try {
            InputStream ims = context.getAssets().open("maps/" + packets[position].getMap() + "/minimap" + packets[position].getMapSize() + ".png");
            imageView.setImageDrawable(Drawable.createFromStream(ims, null));
        }
        catch(IOException ex) {
            Log.e(LOG_TAG, "Image for " + packets[position].getMap() + " " + packets[position].getMapSize() + " not found", ex);
        }

        return rowView;

    };
}