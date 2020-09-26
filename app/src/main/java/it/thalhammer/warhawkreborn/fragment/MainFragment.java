package it.thalhammer.warhawkreborn.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.ServerSearchResponder;
import it.thalhammer.warhawkreborn.model.ServerList;

public class MainFragment extends Fragment implements ServerSearchResponder.OnStateChangeListener {

    /*private class MyWebViewClient extends WebViewClient {
        public boolean failed = false;
        public WebView myWebView;
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("", "Load finished:" + url);
            if(failed) myWebView.setVisibility(View.INVISIBLE);
            else myWebView.setVisibility(View.VISIBLE);
            myWebView.setBackgroundColor(Color.TRANSPARENT);
            myWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e("", "onReceivedError");
            failed = true;
        }

        @Override
        public void onReceivedHttpError(
                WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.e("", "onReceivedHttpError");
            failed = true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e("", "onReceivedSslError");
            failed = true;
        }
    }

    private MyWebViewClient viewClient;*/

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
    public void onViewCreated(View view, Bundle savedInstance) {
        updateDisplay();
        /*final WebView myWebView = (WebView)getView().findViewById(R.id.fragment_main_webview);
        viewClient = new MyWebViewClient();
        viewClient.myWebView = myWebView;
        viewClient.failed = false;
        myWebView.loadUrl("https://warhawk.thalhammer.it/api/app/news");
        myWebView.clearCache(true);
        myWebView.setBackgroundColor(Color.TRANSPARENT);
        myWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        myWebView.setWebViewClient(viewClient);*/
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
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.fragment_main_menu_action_reload){
            ServerSearchResponder.getInstance().updateServers();
            //viewClient.failed = false;
            //viewClient.myWebView.reload();
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
                Activity a = getActivity();
                if(a == null) return;
                final ConnectivityManager manager = (ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);
                final android.net.NetworkInfo connection = manager == null ? null : manager.getActiveNetworkInfo();
                ServerSearchResponder responder = ServerSearchResponder.getInstance();
                if(responder == null) return;
                String statusText = responder.getStatusText();
                int status = R.drawable.ic_status_failed;
                if(responder.isOK()) {
                    status = R.drawable.ic_status_ok;
                    if(connection != null && connection.getType() != ConnectivityManager.TYPE_WIFI && connection.getType() != ConnectivityManager.TYPE_ETHERNET) {
                        status = R.drawable.ic_status_failed;
                        statusText += "\n" + getContext().getString(R.string.fragment_main_no_wifi);
                    }
                }
                ImageView iv = ((ImageView)view.findViewById(R.id.fragment_main_status_image));
                if(iv != null) iv.setImageDrawable(ContextCompat.getDrawable(a, status));
                TextView tv = ((TextView)view.findViewById(R.id.fragment_main_status_text));
                if(tv != null) tv.setText(statusText);
            }
        });
    }
}
