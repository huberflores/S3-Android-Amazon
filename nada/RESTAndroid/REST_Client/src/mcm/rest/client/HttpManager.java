package mcm.rest.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.io.HttpResponseParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import android.widget.Toast;

public class HttpManager {
	private static HttpManager instance;
	protected String EMPTY = "";
	protected final static String PROXY = "193.40.5.245";
	protected final static int PORT = 3128;

	private HttpManager() {

	}

	public static HttpManager getInstance() {
		if (instance == null)
			instance = new HttpManager();
		return instance;
	}
		
	protected HttpClient setProxy(HttpClient httpClient){
		HttpHost proxy = new HttpHost(PROXY, PORT, "http");
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);
		return httpClient;
	}

	public boolean postGetString(String URL, ArrayList<NameValuePair> parameters)
			throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(URL);
		httpClient = setProxy(httpClient);
		
		try {

			HttpEntity entity = null;
			entity = new UrlEncodedFormEntity(parameters);

			httppost.addHeader(entity.getContentType());
			httppost.setEntity(entity);
			HttpResponse response = httpClient.execute(httppost);

			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			String value;
			while ((value = reader.readLine()) != null) {
				value = value + "";
			}
			return true;
		} catch (ClientProtocolException e) {
			String es = e.toString();
			return false;
		} catch (IOException e) {
			String es = e.toString();
			return false;
		}
	}

	protected ArrayList buildParams(String[] keys, String[] values) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		for (int i = 0; i < keys.length; i++)
			params.add(new BasicNameValuePair(keys[i], values[i]));
		return params;
	}

	public boolean UploadFile(File file, String URL) throws IOException {
		HttpClient client = new DefaultHttpClient();
		
		HttpPost post = new HttpPost(URL);

		
		FileBody bin = new FileBody(file);
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart("file", bin);
		post.setEntity(reqEntity);
		HttpResponse response = client.execute(post);
		
		
		int status = response.getStatusLine().getStatusCode();
		HttpEntity resEntity = response.getEntity();
		
		if (resEntity != null) {    
             Log.i("RESPONSE",EntityUtils.toString(resEntity));
        }
		return true;
	}

	
	
}
