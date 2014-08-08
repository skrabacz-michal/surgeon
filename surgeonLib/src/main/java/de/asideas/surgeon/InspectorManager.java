package de.asideas.surgeon;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import de.asideas.overlay.controls.internal.ccontrols.PieController;
import de.asideas.overlay.controls.internal.ccontrols.PieItem;
import de.asideas.overlay.controls.internal.ccontrols.PieRenderer;
import de.asideas.overlay.controls.services.InspectorArcService;
import de.asideas.surgeon.internal.SurgeonManager;

/**
 * Created by mskrabacz on 30/07/14.
 */
public class InspectorManager
{
    private static float FLOAT_PI_DIVIDED_BY_TWO = (float) Math.PI / 2;

    private final static float SWEEP = FLOAT_PI_DIVIDED_BY_TWO / 2;

    private SharedPreferences mSharedPreferences;

    private boolean mBound;

    private final Application mApplication;

    private InspectorArcService.InspectorBinder mBinder;

    private PieRenderer mPieRenderer;

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBinder = (InspectorArcService.InspectorBinder) service;

            mPieRenderer = mBinder.getRenderer();

            createMenu();

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBound = false;
        }
    };

    private SurgeonManager mSurgeonManager;

    public InspectorManager(Application application)
    {
        mApplication = application;
        mSharedPreferences = mApplication.getSharedPreferences("surgeon", Context.MODE_PRIVATE);
    }

    public void bind()
    {
        Intent intent = new Intent(mApplication, InspectorArcService.class);
        mApplication.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbind()
    {
        if (mBound)
        {
            mApplication.unbindService(mConnection);
            mBound = false;
        }
    }

    public void stop()
    {
        mApplication.stopService(new Intent(mApplication, InspectorArcService.class));
    }

    private void createMenu()
    {
        final PieController pieController = new PieController(mApplication, mPieRenderer);

        final PieItem itemStart = pieController.makeItem(R.drawable.running);
        final PieItem itemStop = pieController.makeItem(R.drawable.stop_running);

        itemStop.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 2 * SWEEP, SWEEP);
        itemStop.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                saveState(mSurgeonManager.toggle());

                mPieRenderer.addItem(itemStart);
                mPieRenderer.removeItem(itemStop);
            }
        });

        final PieItem itemShowViews = pieController.makeItem(R.drawable.show_views);
        itemShowViews.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + SWEEP, SWEEP);
        itemShowViews.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                mSurgeonManager.toggleViews();
            }
        });

        final PieItem itemShowDetails = pieController.makeItem(R.drawable.show_details);
        itemShowDetails.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO, SWEEP);
        itemShowDetails.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                mSurgeonManager.toggleMoreInfo();
            }
        });

        final PieItem itemLayers = pieController.makeItem(R.drawable.layers);
        itemLayers.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - SWEEP, SWEEP);

        PieItem itemClose = pieController.makeItem(R.drawable.close);
        itemClose.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - 2 * SWEEP, SWEEP);
        itemClose.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                unbind();
                stop();
            }
        });

        PieItem itemRecord = pieController.makeItem(R.drawable.drag);
        itemRecord.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 3 * SWEEP, SWEEP);
        itemRecord.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mApplication.startActivity(intent);
            }
        });

        itemStart.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 2 * SWEEP, SWEEP);
        itemStart.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                saveState(mSurgeonManager.toggle());

                mPieRenderer.removeItem(itemStart);
                mPieRenderer.addItem(itemStop);

                toggleDependentViews(true, itemShowViews, itemShowDetails, itemLayers);
            }
        });

        boolean state = getState();
        if (!state)
        {
            mPieRenderer.addItem(itemStart);
        }
        else
        {
            mPieRenderer.addItem(itemStop);
        }

        mPieRenderer.addItem(itemShowViews);
        mPieRenderer.addItem(itemShowDetails);
        mPieRenderer.addItem(itemLayers);
        mPieRenderer.addItem(itemClose);

        toggleDependentViews(state, itemShowViews, itemShowDetails, itemLayers);

        // TODO msq
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
//        {
////            mPieRenderer.addItem(itemRecord);
//        }

        PieItem itemLayersHide = pieController.makeItem(R.drawable.remove_layer);
        itemLayersHide.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - SWEEP, SWEEP);
        itemLayersHide.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                mSurgeonManager.removeLayer();
            }
        });
        PieItem itemLayersShow = pieController.makeItem(R.drawable.show_layer);
        itemLayersShow.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO, SWEEP);
        itemLayersShow.setOnClickListener(new PieItem.OnClickListener()
        {
            @Override
            public void onClick(PieItem item)
            {
                mSurgeonManager.showLayer();
            }
        });

        itemLayers.addItem(itemLayersHide);
        itemLayers.addItem(itemLayersShow);
    }

    private void saveState(boolean enable)
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("pie_start", enable);
        editor.apply();
    }

    private boolean getState()
    {
        return mSharedPreferences.getBoolean("pie_start", false);
    }

    public void setSurgeonManager(SurgeonManager surgeonManager)
    {
        mSurgeonManager = surgeonManager;
    }

    private void toggleDependentViews(boolean status, PieItem... items)
    {
        for (PieItem item : items)
        {
            item.setEnabled(status);
        }
    }
}
