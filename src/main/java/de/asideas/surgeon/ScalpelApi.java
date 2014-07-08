package de.asideas.surgeon;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import de.asideas.surgeon.services.InspectorArcService;

/**
 * Created by mskrabacz on 04/07/14.
 */
public class ScalpelApi
{
    private static final String TAG = ScalpelApi.class.getSimpleName();

    private static Application sApplication;

    private static ScalpelManager sScalpelManger;

    private static int sStarted;

    private static int sStopped;

    private static boolean sConfigurationChanged;

    private static Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks()
    {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState)
        {
            Log.d(TAG, "onActivityCreated");

            sScalpelManger = new ScalpelManager(activity);
        }

        @Override
        public void onActivityStarted(Activity activity)
        {
            Log.d(TAG, "onActivityStarted");

            sScalpelManger.injectScalpel();

            ++sStarted;
        }

        @Override
        public void onActivityResumed(Activity activity)
        {
            Log.d(TAG, "onActivityResumed");

            InspectorArcService.setScalpelManager(sScalpelManger);
        }

        @Override
        public void onActivityPaused(Activity activity)
        {
            Log.d(TAG, "onActivityPaused");
        }

        @Override
        public void onActivityStopped(Activity activity)
        {
            Log.d(TAG, "onActivityStopped");

            ++sStopped;
            boolean enteredBackground = sStarted == sStopped;
            if (enteredBackground)
            {
                if (sConfigurationChanged)
                {
                    sConfigurationChanged = false;
                }
                else
                {
                    onEnteredBackground();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState)
        {
            Log.d(TAG, "onActivitySaveInstanceState");
        }

        @Override
        public void onActivityDestroyed(Activity activity)
        {
            Log.d(TAG, "onActivityDestroyed");
        }
    };

    private static ComponentCallbacks componentCallbacks = new ComponentCallbacks()
    {
        @Override
        public void onConfigurationChanged(Configuration newConfig)
        {
            sConfigurationChanged = true;
        }

        @Override
        public void onLowMemory()
        {

        }
    };


    public static void start(Application application)
    {
        ScalpelApi.sApplication = application;
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        application.registerComponentCallbacks(componentCallbacks);
    }

    private static void onEnteredBackground()
    {
        stopService(sApplication);
    }

    public static void stopService(Application application)
    {
        if (application != null)
        {
            application.getApplicationContext().stopService(new Intent(application, InspectorArcService.class));
        }
    }

    public static void stop(Application application)
    {
        if (application != null)
        {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
            application.unregisterComponentCallbacks(componentCallbacks);

            stopService(application);
        }
    }
}
