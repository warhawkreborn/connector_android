package it.thalhammer.warhawkreborn.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import it.thalhammer.warhawkreborn.R;
import it.thalhammer.warhawkreborn.Util;

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
        TextView tv = (TextView)view.findViewById(R.id.fragment_privacy_policy_text);
        tv.setText(Html.fromHtml(Util.readAssetFile("privacy_policy.html", this.getContext())));
    }
}
