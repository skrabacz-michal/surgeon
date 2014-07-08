package de.asideas.surgeon;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.asideas.surgeon.internal.views.ScalpelFrameLayout;
import de.asideas.surgeon.services.InspectorArcService;

/**
 * Created by mskrabacz on 03/07/14.
 */
public class ScalpelManager
{
    private static final String TAG = ScalpelManager.class.getSimpleName();

    private final Activity mContext;

    private ScalpelFrameLayout mScalpelView;

    public ScalpelManager(Activity context)
    {
        mContext = context;
    }

    public void injectScalpel()
    {
        if (BuildConfig.ENABLE_CALABASH)
        {
            ViewGroup rootView = (ViewGroup) mContext.findViewById(android.R.id.content).getRootView();
            int count = rootView.getChildCount();
            if (count > 0)
            {
                drawArc();

                View content = rootView.getChildAt(0);

                rootView.removeView(content);

                mScalpelView = (ScalpelFrameLayout) LayoutInflater.from(mContext).inflate(R.layout.surgeon_wrapper, rootView, false);
                mScalpelView.addView(content);

                rootView.addView(mScalpelView);

                Log.d(TAG, "Calabash mode activated");
            }
            else
            {
                Log.d(TAG, "Activity doesn't set content view ");
            }
        }
    }

    private void drawArc()
    {
        mContext.startService(new Intent(mContext, InspectorArcService.class));
    }

    public void toggle()
    {
        mScalpelView.setLayerInteractionEnabled(!mScalpelView.isLayerInteractionEnabled());
    }

    public void toggleMoreInfo()
    {
        mScalpelView.setDrawIds(!mScalpelView.isDrawingIds());
    }

    public void toggleViews()
    {
        mScalpelView.setDrawViews(!mScalpelView.isDrawingViews());
    }

    public void removeLayer()
    {
        mScalpelView.hideLayers();
    }
}
