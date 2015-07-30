package info.justaway.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import info.justaway.widget.ScaleImageView;

public class ScaleImageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();

        ScaleImageView imageView = new ScaleImageView(activity);
        imageView.setActivity(activity);
        String imageUrl = getArguments().getString("url");

        ImageLoader.getInstance().displayImage(imageUrl, imageView);

        return imageView;
    }
}
