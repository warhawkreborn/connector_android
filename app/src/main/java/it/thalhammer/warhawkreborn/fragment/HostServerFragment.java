package it.thalhammer.warhawkreborn.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import it.thalhammer.warhawkreborn.*;
import it.thalhammer.warhawkreborn.model.AddHostResponse;
import it.thalhammer.warhawkreborn.model.CheckForwardingResponse;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.*;
import java.util.Map;

public class HostServerFragment extends FragmentBase {
    private DiscoveryPacket packet;

    public HostServerFragment() {
        // Required empty public constructor
    }

    static HostServerFragment newInstance(DiscoveryPacket pkt, Inet4Address addr) {
        HostServerFragment fragment = new HostServerFragment();
        Bundle data = new Bundle();
        data.putByteArray("pkt", pkt.getBytes());
        data.putString("ps3", addr.getHostAddress());
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.packet = new DiscoveryPacket(getArguments().getByteArray("pkt"));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private static class Task extends AsyncTask<Void, String, Void> {
        private HostServerFragment parent;

        Task(HostServerFragment parent) {
            this.parent = parent;
        }

        private boolean tryUPNP(String addr) {
            try {
                publishProgress(parent.getString(R.string.fragment_host_server_searching_gateway));

                GatewayDiscover gatewayDiscover = new GatewayDiscover();
                Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

                if (gateways.isEmpty()) {
                    publishProgress(parent.getString(R.string.fragment_host_server_no_gateway));
                    return false;
                }

                GatewayDevice activeGW = gatewayDiscover.getValidGateway();
                if (null != activeGW) {
                    URL uri = new URL(activeGW.getLocation());
                    publishProgress(parent.getString(R.string.fragment_host_server_using_gateway, activeGW.getFriendlyName(), uri.getHost()));
                } else {
                    publishProgress(parent.getString(R.string.fragment_host_server_no_active_gateway));
                    return false;
                }

                try {
                    if (activeGW.addPortMapping(10029, 10029, addr, "UDP", "Warhawk Server redirect")) {
                        publishProgress(parent.getString(R.string.fragment_host_server_mapping_success));
                        return true;
                    } else {
                        publishProgress(parent.getString(R.string.fragment_host_server_mapping_failed));
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final Bundle args = parent.getArguments();
            if(args == null) return null;
            final String ps3 = args.getString("ps3");
            publishProgress(parent.getString(R.string.fragment_host_server_setting_up, ps3));
            boolean upnpres = tryUPNP(ps3);
            if(!upnpres) {
                publishProgress(parent.getString(R.string.fragment_host_server_upnp_failed, ps3));
            }
            while(!this.isCancelled()) {
                CheckForwardingResponse resp = API.checkForwarding();
                if(resp.getInfo().getState().equals("online")) break;
                publishProgress(parent.getString(R.string.fragment_host_server_waiting));
                try {
                    Thread.sleep(30*1000);
                } catch (InterruptedException e) {
                }
            }
            if(this.isCancelled()) return null;

            publishProgress(parent.getString(R.string.fragment_host_server_adding));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
            String fcmId = prefs.getString(parent.getString(R.string.pref_fcm_id), null);
            AddHostResponse resp = API.addHost(true, fcmId);
            if(resp == null) {
                publishProgress(parent.getString(R.string.fragment_host_server_api_request_failed));
                return null;
            }
            if(!resp.isOk()) {
                publishProgress(parent.getString(R.string.fragment_host_server_api_failed, resp.getState()));
                return null;
            }
            publishProgress(parent.getString(R.string.fragment_host_server_added, resp.getInfo().getName()));
            ServerSearchResponder.getInstance().updateServers();
            return null;
        }

        @Override
        protected void  onProgressUpdate(String... logs) {
            final View view = parent.getView();
            if(view == null) return;
            for(String s : logs) {
                ((TextView)view.findViewById(R.id.fragment_host_server_log)).append(s + "\n");
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            final View view = parent.getView();
            if(view == null) return;
            view.findViewById(R.id.fragment_host_server_pb).setVisibility(View.GONE);
        }
    }

    private Task task;

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if(view != null)
            ((TextView)view.findViewById(R.id.fragment_host_server_log)).setText("");
        task = new Task(this);
        task.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }
}
