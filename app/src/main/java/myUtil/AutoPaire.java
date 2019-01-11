package myUtil;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AutoPaire {

    private static Boolean returnValue = false;

    static public boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception
    {
        Method createBondMethod = btClass.getMethod("createBond");
        returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue;
    }
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception
    {
        Method removeBondMethod = btClass.getMethod("removeBond");
        returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue;
    }
    static public boolean setPin(Class btClass, BluetoothDevice btDevice, String str)
            throws Exception
    {
        try
        {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",
            new Class[]{byte[].class});
            returnValue = (Boolean) removeBondMethod.invoke(btDevice,
            new Object[]{str.getBytes()});
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return returnValue;
    }
        // 取消用户输入
    static public boolean cancelPairingUserInput(Class btClass, BluetoothDevice device)
        throws Exception
    {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue;
    }
        // 取消配对
    static public boolean cancelBondProcess(Class btClass, BluetoothDevice device)
        throws Exception
    {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue;
    }
    static public void printAllInform(Class clsShow)
    {
        try
        {
            // 取得所有方法
            Method[]hideMethod = clsShow.getMethods();
            int i= 0;
            for (;i < hideMethod.length; i++){
                Log.e("Methodname",hideMethod[i].getName() + ";and the i is:" +i);

            }
            Field[]allFields = clsShow.getFields();
            for (i= 0;i < allFields.length; i++)
            {
                Log.e("Fieldname",allFields[i].getName());
            }
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
