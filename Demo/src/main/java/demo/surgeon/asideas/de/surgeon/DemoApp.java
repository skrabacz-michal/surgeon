package demo.surgeon.asideas.de.surgeon;

import android.app.Application;
import android.bluetooth.BluetoothClass;
import de.asideas.surgeon.SurgeonApi;

/**
 * Created by mskrabacz on 14/07/14.
 */
public class DemoApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        SurgeonApi.start(this);
    }


    @Override
    public void onTerminate()
    {
        super.onTerminate();

        SurgeonApi.stop(this);
    }
}
