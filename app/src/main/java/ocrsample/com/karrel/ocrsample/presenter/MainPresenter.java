package ocrsample.com.karrel.ocrsample.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.gun0912.tedpermission.PermissionListener;

import gun0912.tedbottompicker.TedBottomPicker;

/**
 * Created by Rell on 2017. 11. 30..
 */

public interface MainPresenter {
    void getImage();

    void precessOcr();

    PermissionListener permissionListener();

    void onCreate();

    TedBottomPicker.OnImageSelectedListener imageSelectedListener();

    interface View {

        void popupBottomSheet();

        void checkPermission(String... permissions);

        Context getContext();

        void setBitmap(Bitmap selectedImage);

        void showError(String s);

        void showRecognizedText(String recognizedText);

        void showProgress();

        void hideProgress();
    }
}
