package de.asideas.surgeon.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
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

    private PieRenderer pieRenderer;

    private RenderOverlay renderOverlay;

    private Point mCenterPoint;

    private View mControlHint;

    private View mDragHint;

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
        mControlHint = frame.findViewById(R.id.control_hint);
        mDragHint = frame.findViewById(R.id.control_drag);

        wm.addView(frame, params);

        createMenu();

        frame.findViewById(R.id.control_hint).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                toggleVisibility(View.INVISIBLE);

                return pieRenderer.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (frame != null)
        {
            wm.removeView(frame);
            frame = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float x = event.getRawX();
            float y = event.getRawY();

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
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float x = event.getRawX();
            float y = event.getRawY();

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

        renderOverlay = (RenderOverlay) LayoutInflater.from(getApplicationContext()).inflate(R.layout.render_overlay, frame, false);

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

        pieRenderer.addItem(itemStart);
        pieRenderer.addItem(itemShowViews);
        pieRenderer.addItem(itemShowDetails);
        pieRenderer.addItem(itemLayers);
        pieRenderer.addItem(itemClose);

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
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        frame.addView(renderOverlay, relativeParams);
    }

    @Override
    public void onPieClosed()
    {
        if (frame != null)
        {
            frame.removeView(renderOverlay);

            // TODO msq
            toggleVisibility(View.VISIBLE);

            Animation fadeOut = new AlphaAnimation(0, 1);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setFillBefore(true);
            fadeOut.setDuration(1600);
            mControlHint.startAnimation(fadeOut);
            mDragHint.startAnimation(fadeOut);
        }
    }

    private void toggleVisibility(int visibility)
    {
        mControlHint.clearAnimation();
        mDragHint.clearAnimation();

        frame.findViewById(R.id.control_drag).setVisibility(visibility);
        frame.findViewById(R.id.control_hint).setVisibility(visibility);
    }
}