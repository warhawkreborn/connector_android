package it.thalhammer.warhawkreborn.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.crashlytics.android.Crashlytics;
import it.thalhammer.warhawkreborn.R;

public class PrivacyPolicyFragment extends FragmentBase {

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        WebView wv = ((WebView)view.findViewById(R.id.fragment_privacy_policy_webview));
        try {
            wv.loadUrl("file:///android_asset/privacy_policy.html");
        }
        catch(Exception ex) {
            wv.loadData("<html><body><h1>Failed to load</h1><br><a href=\"https://warhawk.thalhammer.it/privacy_policy.html\">Online version</a></body></html>", "text/html; charset=utf-8", "utf-8");
            Crashlytics.logException(ex);
        }
    }
}
