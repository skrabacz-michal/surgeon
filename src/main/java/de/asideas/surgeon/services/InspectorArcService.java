package de.asideas.surgeon.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import de.asideas.surgeon.R;
import de.asideas.surgeon.SurgeonManager;
import de.asideas.surgeon.internal.Utils;
import de.asideas.surgeon.internal.ccontrols.PieController;
import de.asideas.surgeon.internal.ccontrols.PieItem;
import de.asideas.surgeon.internal.ccontrols.PieRenderer;
import de.asideas.surgeon.internal.ccontrols.RenderOverlay;

/**
 * Created by mskrabacz on 28/05/14.
 */
public class InspectorArcService extends Service implements View.OnTouchListener, PieRenderer.PieListener
{
    private static final String TAG = InspectorArcService.class.getSimpleName();

    private static final int LONG_PRESS_EVENT = 0x10;

    private static final String MOTION_EVENT_KEY = "motion_event_key";

    private static final double MIN_MOVE_DISTANCE = 0.5f;

    private static float FLOAT_PI_DIVIDED_BY_TWO = (float) Math.PI / 2;

    private final static float sweep = FLOAT_PI_DIVIDED_BY_TWO / 2;

    private static SurgeonManager scalpelManager;

    private float offsetX;

    private float offsetY;

    private int originalXPos;

    private int originalYPos;

    private boolean moving;

    private WindowManager wm;

    private ViewGroup frame;

    private ViewGroup pieWrapper;

    private PieRenderer pieRenderer;

    private Point mCenterPoint;

    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case LONG_PRESS_EVENT:
                    toggleVisibility(View.INVISIBLE);

                    MotionEvent event = msg.getData().getParcelable(MOTION_EVENT_KEY);
                    pieRenderer.onTouchEvent(event);

                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mCenterPoint = Utils.getScreenCenterPoint(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;

        frame = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(R.layout.pie_layout, null, false);
        frame.setOnTouchListener(this);

        wm.addView(frame, params);

        createMenu();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            if (frame != null)
            {
                wm.removeViewImmediate(frame);
                frame = null;
            }
            if (pieWrapper != null)
            {
                wm.removeViewImmediate(pieWrapper);
                pieWrapper = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private float x;

    private float y;

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (pieRenderer.isVisible())
        {
            return pieRenderer.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            x = event.getRawX();
            y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            frame.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

            // TODO msq -
            if (pieRenderer != null && pieRenderer.isVisible())
            {
                pieRenderer.hide();
            }

            Message msg = handler.obtainMessage(LONG_PRESS_EVENT);
            Bundle data = new Bundle();
            data.putParcelable(MOTION_EVENT_KEY, MotionEvent.obtain(event));
            msg.setData(data);

            handler.sendMessageDelayed(msg, 400);
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float dx = event.getRawX() - x;
            float dy = event.getRawY() - y;

            float value = x * dx + y * dy;
            value = value < 0 ? value * -1 : value;
            double distance = Math.sqrt(value);

            if (distance > MIN_MOVE_DISTANCE || distance == Float.NaN)
            {
                x = event.getRawX();
                y = event.getRawY();

                handler.removeMessages(LONG_PRESS_EVENT);

                WindowManager.LayoutParams params = (WindowManager.LayoutParams) frame.getLayoutParams();

                int newX = (int) (offsetX + x);
                int newY = (int) (offsetY + y);

                if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving)
                {
                    return false;
                }

                params.x = (int) (x - (mCenterPoint.x / 2));
                params.y = (int) (y - mCenterPoint.y / 2);

                wm.updateViewLayout(frame, params);
                moving = true;
            }

            x = event.getRawX();
            y = event.getRawY();
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if (moving)
            {
                return true;
            }
        }

        return false;
    }

    public static void setScalpelManager(SurgeonManager scalpelManager)
    {
        InspectorArcService.scalpelManager = scalpelManager;
    }

    private void createMenu()
    {
        pieRenderer = new PieRenderer(getApplicationContext());
        pieRenderer.setPieListener(this);

        final PieController pieController = new PieController(getApplicationContext(), pieRenderer);

        pieWrapper = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(R.layout.render_overlay, frame, false);
        RenderOverlay renderOverlay = (RenderOverlay) pieWrapper.findViewById(R.id.render_overlay);

        final PieItem itemStart = pieController.makeItem(R.drawable.running);
        final PieItem itemStop = pieController.makeItem(R.drawable.stop_running);

        itemStart.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 2 * sweep, sweep);
        itemStart.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.toggle();
                pieRenderer.removeItem(itemStart);
                pieRenderer.addItem(itemStop);
            }
        });

        itemStop.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 2 * sweep, sweep);
        itemStop.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.toggle();
                pieRenderer.addItem(itemStart);
                pieRenderer.removeItem(itemStop);
            }
        });

        PieItem itemShowViews = pieController.makeItem(R.drawable.show_views);
        itemShowViews.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + sweep, sweep);
        itemShowViews.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.toggleViews();
            }
        });

        PieItem itemShowDetails = pieController.makeItem(R.drawable.show_details);
        itemShowDetails.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO, sweep);
        itemShowDetails.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.toggleMoreInfo();
            }
        });

        PieItem itemLayers = pieController.makeItem(R.drawable.layers);
        itemLayers.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - sweep, sweep);

        PieItem itemClose = pieController.makeItem(R.drawable.close);
        itemClose.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - 2 * sweep, sweep);
        itemClose.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                stopSelf();
            }
        });

        PieItem itemRecord = pieController.makeItem(R.drawable.drag);
        itemRecord.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + 3 * sweep, sweep);
        itemRecord.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        pieRenderer.addItem(itemStart);
        pieRenderer.addItem(itemShowViews);
        pieRenderer.addItem(itemShowDetails);
        pieRenderer.addItem(itemLayers);
        pieRenderer.addItem(itemClose);
        pieRenderer.addItem(itemRecord);

        PieItem itemLayersHide = pieController.makeItem(R.drawable.remove_layer);
        itemLayersHide.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO - sweep, sweep);
        itemLayersHide.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.removeLayer();
            }
        });
        PieItem itemLayersShow = pieController.makeItem(R.drawable.show_layer);
        itemLayersShow.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO, sweep);
        itemLayersShow.setOnClickListener(new de.asideas.surgeon.internal.ccontrols.PieItem.OnClickListener()
        {
            @Override
            public void onClick(de.asideas.surgeon.internal.ccontrols.PieItem item)
            {
                scalpelManager.showLayer();
            }
        });

        itemLayers.addItem(itemLayersHide);
        itemLayers.addItem(itemLayersShow);

        renderOverlay.addRenderer(pieRenderer);
    }

    @Override
    public void onPieOpened(int centerX, int centerY)
    {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        wm.addView(pieWrapper, params);
    }

    @Override
    public void onPieClosed()
    {
        if (frame != null)
        {
            wm.removeViewImmediate(pieWrapper);

            toggleVisibility(View.VISIBLE);
        }
    }

    private void toggleVisibility(int visibility)
    {
        frame.findViewById(R.id.control_hint).setVisibility(visibility);
    }
}

