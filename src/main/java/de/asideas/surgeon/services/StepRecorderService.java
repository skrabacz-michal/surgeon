package de.asideas.surgeon.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class StepRecorderService extends AccessibilityService
{
    private static final String TAG = StepRecorderService.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");

        if (getServiceInfo() != null)
        {
            getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        }
    }

    @Override
    protected void onServiceConnected()
    {
        super.onServiceConnected();

        Log.d(TAG, "onServiceConnected");
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        String type = AccessibilityEvent.eventTypeToString(event.getEventType());
        String clazz = event.getClassName().toString();

        String id = "-1";
        AccessibilityNodeInfo source = event.getSource();
        if (source != null)
        {
            id = source.getViewIdResourceName();
        }


        Log.d(TAG, "onAccessibilityEvent " + type + " | " + clazz + " | " + id);
    }

    @Override
    public void onInterrupt()
    {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "onUnbind");

        return super.onUnbind(intent);
    }
}
