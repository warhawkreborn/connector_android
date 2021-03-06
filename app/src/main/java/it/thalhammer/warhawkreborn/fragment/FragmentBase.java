package it.thalhammer.warhawkreborn.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class FragmentBase extends Fragment {
    OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle context) {
        super.onCreate(context);

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
        void setFragment(Fragment f);
        void setFragment(Fragment f, boolean addtobackstack);
        void setDrawerVisible(boolean v);
    }
}
