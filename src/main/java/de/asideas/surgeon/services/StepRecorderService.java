package de.asideas.surgeon.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

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


        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
        {
            String id = getViewId(event);
            String marked = getViewMarked(event);
            String desc = getDesc(event);
            getPositionOnList(event);

            StringBuilder query = new StringBuilder();

//            query.append(type).append(" --> ");
//            boolean sourceAvailable = event.getSource() != null;
//            query.append(sourceAvailable).append(" ---> ");

            query.append("touch(\"").append(clazz).append(" ");
            if (!TextUtils.isEmpty(id))
            {
                query.append("id:'").append(id).append("' ");
            }

            if (!TextUtils.isEmpty(marked))
            {
                query.append("marked:'").append(marked).append("' ");
            }

            if (!TextUtils.isEmpty(desc))
            {
                query.append("contentDescription:'").append(desc).append("' ");
            }

            query.append("\")");

            Log.d(TAG, query.toString());
//            Log.d(TAG, "\n\n");
        }
//
//        Log.d(TAG, "onAccessibilityEvent " + type + " | " + clazz + " | " + id);
    }

    private String getPositionOnList(AccessibilityEvent event)
    {
        String id = "";
        AccessibilityNodeInfo source = event.getSource();
        if (source != null)
        {
            AccessibilityNodeInfo.CollectionItemInfo itemInfo = source.getCollectionItemInfo();
            AccessibilityNodeInfo.CollectionInfo collectionInfo = source.getCollectionInfo();
        }

        return id;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private String getDesc(AccessibilityEvent event)
    {
        String id = "";
        AccessibilityNodeInfo source = event.getSource();
        if (source != null)
        {
            int childCount = source.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                AccessibilityNodeInfo child = source.getChild(i);
                if (child != null)
                {
                    String cid = child.getViewIdResourceName();
                    if (!TextUtils.isEmpty(cid))
                    {
//                        Log.d(TAG, "       " + cid);
                    }
                }
            }

            CharSequence desc = source.getContentDescription();
            if (desc != null)
            {
                id = desc.toString();
            }
        }
        return id;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private String getViewMarked(AccessibilityEvent event)
    {
        String id = "";
        AccessibilityNodeInfo source = event.getSource();
        if (source != null)
        {
            CharSequence text = source.getText();
            if (text != null)
            {
                id = text.toString();
            }
            else
            {
//                if ("android.widget.LinearLayout".equalsIgnoreCase(source.getClassName().toString()))
//                {
//                    int count = source.getChildCount();
//                    AccessibilityNodeInfo child = source.getChild(0);
//                    int childCount = child.getChildCount();
//                    AccessibilityNodeInfo childChild = child.getChild(0);
//                    id = childChild.getText().toString();
//                }
            }
        }
        else
        {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty())
            {
                id = texts.get(0).toString();
            }
        }
        return id;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private String getViewId(AccessibilityEvent event)
    {
        String id = "";
        AccessibilityNodeInfo source = event.getSource();
        if (source != null)
        {
            id = source.getViewIdResourceName();
        }
        return id;
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
