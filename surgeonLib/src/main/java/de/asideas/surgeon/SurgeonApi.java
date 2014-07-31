package de.asideas.surgeon;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import de.asideas.surgeon.internal.SurgeonManager;
import de.asideas.surgeon.services.StepRecorderService;

/**
 * Created by mskrabacz on 04/07/14.
 */
public class SurgeonApi
{
    private static final String TAG = SurgeonApi.class.getSimpleName();

    private static int sStarted;

    private static int sStopped;

    private static boolean sConfigurationChanged;

    private static boolean surgeonEnabled;

    private static Map<String, SurgeonManager> sManagers = new HashMap<String, SurgeonManager>();

    private static Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks()
    {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState)
        {
            Log.d(TAG, "onActivityCreated");

            SurgeonManager scalpelManger = new SurgeonManager(activity);
            sManagers.put(activity.getComponentName().getPackageName() + activity.getComponentName().getClassName(), scalpelManger);
        }

        @Override
        public void onActivityStarted(Activity activity)
        {
            Log.d(TAG, "onActivityStarted");

            sManagers.get(activity.getComponentName().getPackageName() + activity.getComponentName().getClassName()).injectSurgeon();
            ++sStarted;
        }

        @Override
        public void onActivityResumed(Activity activity)
        {
            Log.d(TAG, "onActivityResumed");

            mInspectorManager.setSurgeonManager(sManagers.get(activity.getComponentName().getPackageName() + activity.getComponentName().getClassName()));

            mInspectorManager.bind();
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

            sManagers.remove(activity.getComponentName().getPackageName() + activity.getComponentName().getClassName());
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

    private static InspectorManager mInspectorManager;

    public static void start(Application application)
    {
        surgeonEnabled = checkSettings(application.getPackageName());

        if (surgeonEnabled)
        {
            application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
            application.registerComponentCallbacks(componentCallbacks);
        }

        StepRecorderService.sParentPackageName = application.getPackageName();

        mInspectorManager = new InspectorManager(application);
        mInspectorManager.bind();
    }

    private static void onEnteredBackground()
    {
        mInspectorManager.unbind();
        mInspectorManager.stop();
    }

    public static void stop(Application application)
    {
        if (application != null && surgeonEnabled)
        {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
            application.unregisterComponentCallbacks(componentCallbacks);

            mInspectorManager.unbind();
            mInspectorManager.stop();
        }
    }

    private static boolean checkSettings(String packageName)
    {
        Boolean surgeonEnabled = false;
        try
        {
            Class<?> clazz = Class.forName(packageName + ".BuildConfig");
            Field setting = clazz.getField("ENABLE_SURGEON");
            surgeonEnabled = (Boolean) setting.get(null);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return surgeonEnabled;
    }
}
