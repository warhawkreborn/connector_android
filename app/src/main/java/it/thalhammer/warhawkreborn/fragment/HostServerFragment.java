package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.*;
import it.thalhammer.warhawkreborn.model.AddHostResponse;
import it.thalhammer.warhawkreborn.model.CheckForwardingResponse;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;

public class HostServerFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private DiscoveryPacket packet;

    public HostServerFragment() {
        // Required empty public constructor
    }

    public static HostServerFragment newInstance(DiscoveryPacket pkt, Inet4Address addr) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_server, container, false);
    }

    private class Task extends AsyncTask<Void, String, Void> {
        private boolean tryUPNP(String addr) {
            try {
                publishProgress("Searching for gateway...\n");

                GatewayDiscover gatewayDiscover = new GatewayDiscover();
                Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

                if (gateways.isEmpty()) {
                    publishProgress("No gateway found :(\n");
                    return false;
                }

                GatewayDevice activeGW = gatewayDiscover.getValidGateway();
                if (null != activeGW) {
                    URL uri = new URL(activeGW.getLocation());
                    publishProgress("Using gateway: " + activeGW.getFriendlyName() + "(" + uri.getHost() + ")\n");
                } else {
                    publishProgress("No active gateway device found :(\n");
                    return false;
                }

                try {
                    PortMappingEntry portMapping = new PortMappingEntry();

                    if (activeGW.addPortMapping(10029, 10029, addr, "UDP", "Warhawk Server redirect")) {
                        publishProgress("Mapping SUCCESSFUL.\n");
                        return true;
                    } else {
                        publishProgress("Failed to create port mapping.\n");
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
        protected Void doInBackground(Void... voids) {
            String ps3 = getArguments().getString("ps3");
            boolean upnpres = tryUPNP(ps3);
            if(!upnpres) {
                publishProgress("UPNP setup failed.\nPlease create a port forward in your router:\n");
                publishProgress("External Port: 10029\nInternal Port 10029\nInternal IP: " + ps3 + "\nType: UDP\n");
            }
            while(!this.isCancelled()) {
                CheckForwardingResponse resp = API.checkForwarding();
                if(resp.getInfo().getState().equals("online")) break;
                publishProgress("Waiting for host to become available, sleeping 30 seconds.\n");
                try {
                    Thread.sleep(30*1000);
                } catch (InterruptedException e) {
                }
            }
            if(this.isCancelled()) return null;

            publishProgress("Host is online, adding to registry\n");

            AddHostResponse resp = API.addHost(true);
            if(resp == null) {
                publishProgress("Failed to add host to registry :(\n");
                publishProgress("API Request failed\n");
                return null;
            }
            if(!resp.isOk()) {
                publishProgress("Failed to add host to registry :(\n");
                publishProgress(resp.getState() + "\n");
                return null;
            }
            publishProgress("Added server " + resp.getInfo().getName() + " to registry.\n");
            publishProgress("It should show up in the server list!\n");
            return null;
        }

        @Override
        protected void  onProgressUpdate(String... logs) {
            for(String s : logs) {
                ((TextView)getView().findViewById(R.id.fragment_host_server_log)).append(s);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            getView().findViewById(R.id.fragment_host_server_pb).setVisibility(View.GONE);
        }
    }

    private Task task;

    @Override
    public void onResume() {
        super.onResume();
        ((TextView)getView().findViewById(R.id.fragment_host_server_log)).setText("Setting up server configuration for PS3 on IP " + getArguments().getString("ps3") + "\n");
        task = new Task();
        task.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
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
