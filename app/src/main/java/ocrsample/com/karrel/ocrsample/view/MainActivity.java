package ocrsample.com.karrel.ocrsample.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.TedPermission;

import gun0912.tedbottompicker.TedBottomPicker;
import ocrsample.com.karrel.ocrsample.R;
import ocrsample.com.karrel.ocrsample.databinding.ActivityMainBinding;
import ocrsample.com.karrel.ocrsample.presenter.MainPresenter;
import ocrsample.com.karrel.ocrsample.presenter.MainPresenterImpl;
import ocrsample.com.karrel.ocrsample.util.BFDialog;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    private String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        presenter = new MainPresenterImpl(this);
        presenter.onCreate();
        setupEvents();
    }

    private void setupEvents() {
        // get image
        binding.getImage.setOnClickListener(v -> presenter.getImage());

        // process ocr
        binding.processOcr.setOnClickListener(v -> presenter.precessOcr());
    }

    @Override
    public void popupBottomSheet() {
        TedBottomPicker picker = new TedBottomPicker.Builder(this)
                .setOnImageSelectedListener(presenter.imageSelectedListener()).create();

        picker.show(getSupportFragmentManager());
    }

    @Override
    public void checkPermission(String... permissions) {
        TedPermission.with(this)
                .setPermissionListener(presenter.permissionListener())
                .setPermissions(permissions)
                .check();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setBitmap(Bitmap bm) {
        binding.imageView.setImageBitmap(bm);
    }

    @Override
    public void showError(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRecognizedText(String recognizedText) {
        BFDialog.newInstance(this).showSimpleDialog(recognizedText);
        Log.e(TAG, String.format("showRecognizedText(%s)", recognizedText));
    }

    @Override
    public void showProgress() {
        binding.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        binding.progress.setVisibility(View.GONE);
    }
}
