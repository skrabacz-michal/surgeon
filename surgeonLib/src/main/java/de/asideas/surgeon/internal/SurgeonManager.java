package de.asideas.surgeon.internal;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.asideas.surgeon.R;
import de.asideas.surgeon.internal.views.SurgeonFrameLayout;

/**
 * Created by mskrabacz on 03/07/14.
 */
public class SurgeonManager
{
    private static final String TAG = SurgeonManager.class.getSimpleName();

    private final Activity mContext;

    private SurgeonFrameLayout mSurgeonView;

    public SurgeonManager(Activity context)
    {
        mContext = context;
    }

    public void injectSurgeon()
    {
        ViewGroup rootView = (ViewGroup) mContext.findViewById(android.R.id.content).getRootView();
        int count = rootView.getChildCount();
        if (count > 0)
        {
            View content = rootView.getChildAt(0);
            if (!(content instanceof SurgeonFrameLayout))
            {
                rootView.removeView(content);

                mSurgeonView = (SurgeonFrameLayout) LayoutInflater.from(mContext).inflate(R.layout.surgeon_wrapper, rootView, false);
                mSurgeonView.addView(content);

                rootView.addView(mSurgeonView);
            }

            Log.d(TAG, "Calabash mode activated");
        }
        else
        {
            Log.d(TAG, "Activity doesn't set content view ");
        }
    }

    public boolean toggle()
    {
        return mSurgeonView.setLayerInteractionEnabled(!mSurgeonView.isLayerInteractionEnabled());
    }

    public void toggleMoreInfo()
    {
        mSurgeonView.setDrawIds(!mSurgeonView.isDrawingIds());
    }

    public void toggleViews()
    {
        mSurgeonView.setDrawViews(!mSurgeonView.isDrawingViews());
    }

    public void removeLayer()
    {
        mSurgeonView.hideLayers();
    }

    public void showLayer()
    {
        mSurgeonView.showLayers();
    }
}
