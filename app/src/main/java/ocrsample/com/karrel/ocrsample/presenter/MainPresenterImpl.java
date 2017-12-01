package ocrsample.com.karrel.ocrsample.presenter;

import android.Manifest;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.gun0912.tedpermission.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import gun0912.tedbottompicker.TedBottomPicker;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Rell on 2017. 11. 30..
 */

public class MainPresenterImpl implements MainPresenter {
    private String TAG = MainPresenterImpl.class.getSimpleName();
    private MainPresenter.View view;
    private Bitmap bitmap;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    final String lang = "kor";
    private boolean isProcessing = false;

    public MainPresenterImpl(View view) {
        this.view = view;
    }

    @Override
    public void getImage() {
        view.popupBottomSheet();
    }

    @Override
    public void precessOcr() {
        if (bitmap == null) {
            // TODO: 2017. 12. 1.
            view.showError("선택된 이미지가 없다.");
            return;
        }

        if (isProcessing) {
            return;
        }
        view.showProgress();
        isProcessing = true;

        Observable observable = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmap);

            String recognizedText = baseApi.getUTF8Text();

            baseApi.end();
            subscriber.onNext(recognizedText);
        });

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        view.hideProgress();
                        isProcessing = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.hideProgress();
                        isProcessing = false;
                    }

                    @Override
                    public void onNext(String recognizedText) {

                        if (lang.equalsIgnoreCase("eng")) {
                            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
                        }

                        recognizedText = recognizedText.trim();

                        if (recognizedText.length() != 0) {
                            view.showRecognizedText(recognizedText);
                        }

                        view.hideProgress();
                        isProcessing = false;
                    }
                });
    }

    @Override
    public PermissionListener permissionListener() {
        return permissionListener;
    }

    @Override
    public void onCreate() {
        view.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    private void initPath() {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = view.getContext().getAssets();
                InputStream in = assetManager.open(lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.e(TAG, "onPermissionGranted()");

            initPath();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Log.e(TAG, "onPermissionDenied()");
        }
    };

    @Override
    public TedBottomPicker.OnImageSelectedListener imageSelectedListener() {
        return onImageSelectedListener;
    }

    private TedBottomPicker.OnImageSelectedListener onImageSelectedListener = uri -> {
        try {
            final Bitmap selectedImage = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(), uri);
            view.setBitmap(bitmap = selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
}
