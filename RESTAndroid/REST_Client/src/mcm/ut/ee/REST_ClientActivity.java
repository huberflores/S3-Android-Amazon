package mcm.ut.ee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


import mcm.rest.client.DataManager;
import mcm.rest.client.HttpManager;
import mcm.rest.client.Utilities;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
 * author Huber Flores
 */

public class REST_ClientActivity extends Activity implements android.view.View.OnClickListener {
    
	
	private File imageFile;
	
	HttpManager httpManager = HttpManager.getInstance();
	public final DataManager data = DataManager.getInstance();
	
	public String getFileName() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMMddHHmmss");
		return formatter.format(currentDate.getTime()) + "IMG.PNG";

	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      
        Button btn_get = (Button) findViewById(R.id.button1);
        btn_get.setOnClickListener(this);
        
        Button btn_post = (Button) findViewById(R.id.button2);
        btn_post.setOnClickListener(this);
       
        Button btn_camera = (Button) findViewById(R.id.button3);
        btn_camera.setOnClickListener(this);
      
    }
    
    public void onClick(View v) {
	
		switch (v.getId()) {
		case R.id.button1:
			//start an instance through MCM
			/*ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
			
			parameters.add(new BasicNameValuePair(Utilities.IMAGE_PARAM, Utilities.image));

			try {
				httpManager.postGetString(Utilities.MCMTPHandlerGET, parameters);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
			Toast.makeText(getApplicationContext(), "Button 1 disabled, uncomment code",Toast.LENGTH_SHORT).show();          	
			
			
			break;
			
			
		case R.id.button2:
			//Upload a specific file through MCM
			//the file to upload can be hard coded here
			
			/*imageFile = new File("/mnt/sdcard/your/path/image.jpg");
			SendHttpRequestTask task = new SendHttpRequestTask();
			task.execute();*/
			
			Toast.makeText(getApplicationContext(), "Button 2 disabled, uncomment code",Toast.LENGTH_SHORT).show();
			
			break;
			
		case R.id.button3:
			//Upload a picture taken from the camera through MCM
			Toast.makeText(getApplicationContext(), "Taking picture",
	                Toast.LENGTH_SHORT).show();          	
			
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, Utilities.CAMERA_PIC_REQUEST);
			
			//Toast.makeText(getApplicationContext(), "Button 3 disabled, uncomment code",Toast.LENGTH_SHORT).show();
			
			break;
		}
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Utilities.CAMERA_PIC_REQUEST) {
			Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			thumbnail.compress(CompressFormat.JPEG, 75, bos);
			byte[] dataArray = bos.toByteArray();
			String fileName = getFileName();
			FileOutputStream fos; 
			try {
				fos = openFileOutput(fileName, MODE_WORLD_WRITEABLE);
				fos.write(dataArray);
				fos.close();
				imageFile = new File(getFilesDir(), fileName);

				SendHttpRequestTask task = new SendHttpRequestTask();
				task.execute();
				
				
				System.out.println(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
    
    
    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
        	
			try {
				httpManager.UploadFile(imageFile,Utilities.MCMTPHandlerPOST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        	
            return null;
        }

        @Override
        protected void onPostExecute(String data) {            
        	Toast.makeText(getApplicationContext(), "File uploaded",Toast.LENGTH_SHORT).show();

        }

    }

    
}