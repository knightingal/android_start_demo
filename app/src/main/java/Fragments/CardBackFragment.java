package Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.jianming.myapplication.CardFlipActivity;
import com.example.jianming.myapplication.R;

public class CardBackFragment extends Fragment {

    private CardFlipActivity activity = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_back, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getActivity().findViewById(R.id.root_view);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.flipCard();
            }
        });
    }

    public void setActivity(CardFlipActivity activity) {
        this.activity = activity;
    }
}
