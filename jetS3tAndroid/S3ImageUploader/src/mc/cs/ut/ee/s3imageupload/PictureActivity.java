package mc.cs.ut.ee.s3imageupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import mc.cs.ut.ee.s3imageupload.R;
import mc.cs.ut.ee.s3imageupload.R.id;
import mc.cs.ut.ee.s3imageupload.R.layout;
import mc.cs.ut.ee.s3imageupload.R.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends Activity {

    private static final int TAKE_PICTURE_RESULT = 42;
    private AWSCredentials credentials;
    private S3Service service = null;
    private Spinner spinner = null;
    private ImageView thumbnail = null;
    private String lastFilePath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        thumbnail = (ImageView) findViewById(R.id.preview);

        Bundle extras = getIntent().getExtras();
        credentials = new AWSCredentials(extras.getString(LogInActivity.EXTRA_USERNAME), extras.getString(LogInActivity.EXTRA_PASSWORD));
        try {
            service = new RestS3Service(credentials);
        } catch (S3ServiceException e) {
            Toast.makeText(getApplicationContext(), "Unable to acquire connection... Exiting.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new ListBucketsTask().execute(new Object());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_log_in, menu);
        return true;
    }

    public void takePicture(@SuppressWarnings("unused") View view) {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TAKE_PICTURE_RESULT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                File dest = getTempImageFile(getApplicationContext());
                lastFilePath = dest.getAbsolutePath();

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                } catch (Exception e) {
                    Log.e("RESULT", e.getMessage());
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }

                if (thumbnail != null && lastFilePath != null) {
                    thumbnail.setImageBitmap(bitmap);
                    thumbnail.invalidate();
                }
            }
            else if (Activity.RESULT_CANCELED == resultCode) {
                Toast.makeText(getApplicationContext(), "Picture capturing was cancelled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadPicture(@SuppressWarnings("unused") View view) {
        if (spinner == null) {
            Toast.makeText(getApplicationContext(), "No bucket selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        S3Bucket selectedBucket = (S3Bucket) spinner.getSelectedItem();
        File imageFile = new File(lastFilePath);
        if (selectedBucket != null && imageFile.exists()) {
            new UploadPictureTask(selectedBucket).execute(imageFile);
        }

    }

    private class ListBucketsTask extends AsyncTask<Object, Integer, S3Bucket[]> {

        private static final String TAG = "ListBucketsTask";

        @Override
        protected S3Bucket[] doInBackground(Object... params) {
            S3Bucket[] result = new S3Bucket[0];
            try {
                result = service.listAllBuckets();
            } catch (S3ServiceException e) {
                Log.e(TAG, "Bucket listing failed: " + e.getErrorMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(S3Bucket[] result) {
            if (result.length < 1) {
                Toast.makeText(getApplicationContext(), "There are no buckets available! Please create one using AWS services.", Toast.LENGTH_SHORT).show();
                return;
            }

            SpinnerAdapter adapter = new ArrayAdapter<S3Bucket>(getApplicationContext(), R.layout.layout_spinner_dropdown_item, result) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = vi.inflate(R.layout.layout_spinner_item, null);
                    }
                    final S3Bucket bucket = getItem(position);
                    if (bucket != null) {
                        TextView name = (TextView) v.findViewById(R.id.name);
                        if (name != null) {
                            name.setText(bucket.getName());
                        }

                        TextView owner = (TextView) v.findViewById(R.id.location);
                        if (owner != null) {
                            owner.setText(bucket.getLocation());
                        }
                    }

                    return v;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = vi.inflate(R.layout.layout_spinner_dropdown_item, null);
                    }
                    final S3Bucket bucket = getItem(position);
                    if (bucket != null) {
                        TextView info = (TextView) v.findViewById(R.id.spinner_dropdown);
                        if (info != null) {
                            info.setText(bucket.getName() + ", " + bucket.getCreationDate() + ", " + bucket.getOwner().getDisplayName());
                        }
                    }

                    return v;
                }

            };

            spinner = (Spinner) findViewById(R.id.bucket_list);
            spinner.setAdapter(adapter);
        }

    }

    private class UploadPictureTask extends AsyncTask<File, Integer, Boolean> {

        private static final String TAG = "UploadPictureTask";
        private final S3Bucket bucket;
        public UploadPictureTask(S3Bucket bucket) {
            this.bucket = bucket;
        }

        @Override
        protected Boolean doInBackground(File... pictures) {
            if (pictures == null || pictures.length != 1) {
                return false;
            }

            try {
                File file = pictures[0];
                String absolutePath = file.getAbsolutePath();
                service.putObject(bucket, new S3Object(file));
                if (!file.delete()) {
                    Log.e(TAG, "Couldn't delete file " + absolutePath);
                }
                return true;
            } catch (S3ServiceException e) {
                Log.e(TAG, e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean uploaded) {
            String message = Boolean.TRUE.equals(uploaded) ? "Image was successfully uploaded" : "Image upload failed";
            Toast.makeText(getApplicationContext(), (message + " (" + bucket.getName() + ")"), Toast.LENGTH_SHORT).show();

        }
    }

    private File getTempImageFile(Context context) {
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, System.currentTimeMillis() + ".png");
    }
}
