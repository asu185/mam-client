package com.example.anextractor_ver1;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlSerializer;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.util.Xml;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint("NewApi")
public class Extractor extends Activity {
	//protected static final boolean D = false;
	Context context = this;
	
	private Button btnUpload;
	private int apknum = 0;
	private int count = 0;
	private Boolean firstTime_upload_flag = true;
	private String xml_name = "/default.xml";
	private String IMEI = "default_IMEI";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this;

		setContentView(R.layout.main);
		setupViewComponent();

	}

	private void setupViewComponent() {
		btnUpload = (Button) findViewById(R.id.btnUpload);
		// btnRefuse = (Button)findViewById(R.id.btnRefuse);

		btnUpload.setOnClickListener(UploadClickListener);
	}

	private Button.OnClickListener UploadClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Log.i("SENSEsysinfo", "SENSEsysinfo begin.");

			// Load all the application information on system
			extract_app_infos();

			String xmldir = Environment.getExternalStorageDirectory() + xml_name;
			//Log.i("SENSEsysinfo", "xmldir: " + xmldir);
			//uploadFiles(xmldir);
			new UploadToServerTask("http://140.113.179.233:3001/upload", xmldir).execute();

			Log.i("SENSEsysinfo", "SENSEsysinfo finish.");
		}
	};

	private void extract_app_infos() {
		Log.i("SENSEsysinfo", "Extracting...");
		final PackageManager pManager = context.getPackageManager();
		TelephonyManager tm = (TelephonyManager) getSystemService(context.TELEPHONY_SERVICE);
		IMEI = tm.getDeviceId();
		xml_name = "/" + IMEI + ".xml";
		
		WifiManager wifimanager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifimanager.getConnectionInfo();

		final List<PackageInfo> appList = pManager.getInstalledPackages(0);

		// create a new file called "new.xml" in the SD card
		File newxmlfile = new File(Environment.getExternalStorageDirectory() + xml_name);
		try {
			newxmlfile.createNewFile();
		} catch (IOException e) {
			Log.e("IOException", "exception in createNewFile() method");
		}
		// we have to bind the new file with a FileOutputStream
		FileOutputStream fileos = null;
		try {
			fileos = new FileOutputStream(newxmlfile);
		} catch (FileNotFoundException e) {
			Log.e("FileNotFoundException", "can't create FileOutputStream");
		}
		// we create a XmlSerializer in order to write xml data

		XmlSerializer serializer = Xml.newSerializer();
		try {

			// we set the FileOutputStream as output for the serializer, using
			// UTF-8 encoding
			serializer.setOutput(fileos, "UTF-8");
			// Write <?xml declaration with encoding (if encoding not null) and
			// standalone flag (if standalone not null)
			serializer.startDocument(null, Boolean.valueOf(true));
			// set indentation option
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			// start a tag called "root"

			// ///////////////////////////////System start
			//String MacAddress = wifiinfo.getMacAddress();

			serializer.startTag(null, "device");

			serializer.startTag(null, "SystemInfo");
			serializer.startTag(null, "IMEI");
			serializer.text(IMEI);
			serializer.endTag(null, "IMEI");
			// Wifi State
			serializer.startTag(null, "Wifi");
			serializer.startTag(null, "status");
			if (wifimanager.isWifiEnabled()) {
				serializer.text("On");
				serializer.endTag(null, "status");
				serializer.startTag(null, "MacAdress");
				serializer.text(wifiinfo.getMacAddress());
				serializer.endTag(null, "MacAdress");
				serializer.startTag(null, "IpAddress");
				serializer.text(Integer.toString(wifiinfo.getIpAddress()));
				serializer.endTag(null, "IpAddress");
				serializer.startTag(null, "LinkSpeed");
				serializer.text(Integer.toString(wifiinfo.getLinkSpeed()));
				serializer.endTag(null, "LinkSpeed");
				serializer.startTag(null, "NetworkId");
				serializer.text(Integer.toString(wifiinfo.getNetworkId()));
				serializer.endTag(null, "NetworkId");
				serializer.startTag(null, "SSID");
				serializer.text(wifiinfo.getSSID());
				serializer.endTag(null, "SSID");
				serializer.startTag(null, "BSSID");
				serializer.text(wifiinfo.getBSSID());
				serializer.endTag(null, "BSSID");
				serializer.startTag(null, "SupplicantState");
				serializer.text(wifiinfo.getSupplicantState().toString());
				serializer.endTag(null, "SupplicantState");
			} else {
				serializer.text("Off");
				serializer.endTag(null, "status");
			}

			serializer.endTag(null, "Wifi");

			serializer.endTag(null, "SystemInfo");

			// //////////////////////////////applist start
			serializer.startTag(null, "applist");
			// i indent code just to have a view similar to xml-tree
			for (int i = 0; i < appList.size(); ++i) {
				PackageInfo appInfo = appList.get(i);
				Drawable appIcon = appInfo.applicationInfo.loadIcon(pManager);
				String appName = appInfo.applicationInfo.loadLabel(
						getPackageManager()).toString();
				String appPName = appInfo.applicationInfo.packageName;
				String appType = "Unknown application";
				String appVer = appInfo.versionName;
				int appVerCode = appInfo.versionCode;
				long appLastUpTime = appInfo.lastUpdateTime;
				String[] rPermission = null;
				ActivityInfo[] activity = null;
				ServiceInfo[] service = null;
				ProviderInfo[] provider = null;
				ActivityInfo[] receiver = null;
				Signature[] signatures = null;
				String shareduid = null;
				String srcdir = appInfo.applicationInfo.sourceDir;

				if(apknum < 2){
					//uploadFiles(srcdir);
					new UploadToServerTask("http://140.113.179.233:3001/upload", srcdir).execute();
					apknum++;
				}
				// Log.i("PackageList", "package: "+appPName +", sourceDir: "+
				// srcdir );
				/*
				 * File src = new File(srcdir); File dst = new
				 * File(Environment.getExternalStorageDirectory
				 * ()+"/tmp/"+appPName+".apk"); try{ dst.createNewFile(); }
				 * catch(IOException e){ Log.e("IOException",
				 * "exception in createNewFile() method"); } //we have to bind
				 * the new file with a FileOutputStream FileOutputStream fileos1
				 * = null; try{ fileos1 = new FileOutputStream(dst); }
				 * catch(FileNotFoundException e){
				 * Log.e("FileNotFoundException",
				 * "can't create FileOutputStream"); }
				 */

				// copy(src, dst);
				// copyFile(src, dst);*/
				try {
					rPermission = pManager.getPackageInfo(appPName,
							PackageManager.GET_PERMISSIONS).requestedPermissions;
					activity = pManager.getPackageInfo(appPName,
							PackageManager.GET_ACTIVITIES).activities;
					service = pManager.getPackageInfo(appPName,
							PackageManager.GET_SERVICES).services;
					provider = pManager.getPackageInfo(appPName,
							PackageManager.GET_PROVIDERS).providers;
					receiver = pManager.getPackageInfo(appPName,
							PackageManager.GET_RECEIVERS).receivers;
					shareduid = pManager.getPackageInfo(appPName,
							PackageManager.GET_SHARED_LIBRARY_FILES).sharedUserId;
					signatures = pManager.getPackageInfo(appPName,
							PackageManager.GET_SIGNATURES).signatures;
				} catch (NameNotFoundException e) {
					System.out.println(e.toString());
				}
				/*
				 * Log.i("SENSEsysinfo", "App name: "+appName);
				 * Log.i("SENSEsysinfo", "App package name: "+appPName);
				 * Log.i("SENSEsysinfo",
				 * "flags: "+appInfo.applicationInfo.flags);
				 * Log.i("SENSEsysinfo",
				 * "FLAG_SYSTEM: "+appInfo.applicationInfo.FLAG_SYSTEM);
				 */
				if ((appInfo.applicationInfo.flags & appInfo.applicationInfo.FLAG_SYSTEM) <= 0) {
					appType = "[User app]";
					// Log.i("SENSEsysinfo", appType);
				} else {
					appType = "[System app]";
					// Log.i("SENSEsysinfo", appType);
				}
				/*
				 * Log.i("SENSEsysinfo", "App version name: "+appVer);
				 * Log.i("SENSEsysinfo", "App version code: "+appVerCode);
				 * Log.i("SENSEsysinfo",
				 * "App last update time: "+appLastUpTime);
				 */
				int permCount = (rPermission == null) ? 0 : rPermission.length;
				// Log.i("SENSEsysinfo", "App permissions: "+permCount);
				/*
				 * for(int j=0; j<permCount; ++j){ Log.i("SENSEsysinfo",
				 * rPermission[j]); }
				 */

				/*
				 * //signature int sigCount=(signatures==null) ? 0 :
				 * signatures.length; //Log.e("SENSEsysinfo",
				 * "Signaturesnum: "+sigCount); for(int j=0; j<sigCount; ++j){
				 * //Log.e("SENSEsysinfo1", signatures[j].toString());
				 * 
				 * }
				 */

				serializer.startTag(null, appPName);
				serializer.startTag(null, "appName");
				serializer.text(appName);
				serializer.endTag(null, "appName");

				serializer.startTag(null, "appType");
				serializer.text(appType);
				serializer.endTag(null, "appType");

				serializer.startTag(null, "appVersion");
				serializer.text("Version: " + appVer);
				serializer.endTag(null, "appVersion");
				serializer.startTag(null, "appPermission");
				for (int j = 0; j < permCount; ++j) {
					// Log.i("SENSEsysinfo", "12"+rPermission[j]);
					if (j != (permCount - 1))
						serializer.text("\n      " + rPermission[j]);

				}
				serializer.text("\n");
				serializer.endTag(null, "appPermission");
				/*
				 * serializer.startTag(null, "Activity"); //activity int
				 * actCount=(activity==null) ? 0 : activity.length;
				 * 
				 * for(int j=0; j<actCount; ++j){
				 * 
				 * if(j != (actCount-1))
				 * serializer.text("\n  -"+activity[j].toString());
				 * 
				 * }
				 * 
				 * serializer.endTag(null, "Activity");
				 * 
				 * serializer.startTag(null, "Service"); //service int
				 * serCount=(service==null) ? 0 : service.length;
				 * //Log.e("SENSEsysinfo", "Servicenum: "+serCount); for(int
				 * j=0; j<serCount; ++j){
				 * 
				 * if(j != (serCount-1))
				 * serializer.text("\n  -"+service[j].toString()); }
				 * serializer.endTag(null, "Service");
				 * 
				 * serializer.startTag(null, "Provider"); //provider int
				 * proCount=(provider==null) ? 0 : provider.length;
				 * //Log.e("SENSEsysinfo", "Providernum: "+proCount); for(int
				 * j=0; j<proCount; ++j){
				 * 
				 * if(j != (proCount-1))
				 * serializer.text("\n  -"+provider[j].toString()); }
				 * serializer.endTag(null, "Provider");
				 * 
				 * serializer.startTag(null, "Receiver");
				 * 
				 * //receiver int recCount=(receiver==null) ? 0 :
				 * receiver.length; //Log.e("SENSEsysinfo",
				 * "Receivernum: "+recCount); for(int j=0; j<recCount; ++j){
				 * 
				 * if(j != (recCount-1))
				 * serializer.text("\n  -"+receiver[j].toString()); }
				 * serializer.endTag(null, "Receiver");
				 */
				serializer.endTag(null, appPName);

				/*
				 * serializer.startTag(null, appName); serializer.startTag(null,
				 * "appPName"); serializer.text(appPName);
				 * serializer.endTag(null, "appPName");
				 * 
				 * serializer.startTag(null, "appType");
				 * serializer.text(appType); serializer.endTag(null, "appType");
				 * 
				 * serializer.startTag(null, "appVersion");
				 * serializer.text("Version: "+appVer); serializer.endTag(null,
				 * "appVersion"); serializer.endTag(null, appName);
				 */

			}
			/*
			 * serializer.startTag(null, "child1"); serializer.endTag(null,
			 * "child1");
			 * 
			 * serializer.startTag(null, "child2"); //set an attribute called
			 * "attribute" with a "value" for <child2>
			 * serializer.attribute(null, "attribute", "value");
			 * serializer.endTag(null, "child2");
			 * 
			 * serializer.startTag(null, "child3"); //write some text inside
			 * <child3> serializer.text("some text inside child3");
			 * serializer.endTag(null, "child3");
			 */

			serializer.endTag(null, "applist");
			serializer.endTag(null, "device");
			serializer.endDocument();
			// write xml data into the FileOutputStream
			serializer.flush();
			// finally we close the file stream
			fileos.close();

		} catch (Exception e) {
			Log.e("Exception", "error occurred while creating xml file");
		}
       
            
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());
    	
		
		Log.i("SENSEsysinfo", "Extracting finished.");
	}

	public void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/*
	 * public static void copyFile(File sourceFile, File destFile) throws
	 * IOException { if(!destFile.exists()) { destFile.createNewFile(); }
	 * 
	 * FileChannel source = null; FileChannel destination = null;
	 * 
	 * try { source = new FileInputStream(sourceFile).getChannel(); destination
	 * = new FileOutputStream(destFile).getChannel();
	 * destination.transferFrom(source, 0, source.size()); } finally { if(source
	 * != null) { source.close(); } if(destination != null) {
	 * destination.close(); } } }
	 */

	public void uploadFiles(final String PathFile) {
		new Thread() {
			@Override
			public void run() {
				super.run();
				Log.i("SENSEsysinfo", "Upload files.");
				count++;
				
				List< NameValuePair> params = new ArrayList< NameValuePair>();
                params.add(new BasicNameValuePair("file", PathFile));
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://140.113.179.233:3001/upload");
                
                try{
                    //setup multipart entity
                    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                    for(int i=0;i< params.size();i++){
                        //identify param type by Key
                        if(params.get(i).getName().equals("file")){
                            File f = new File(params.get(i).getValue());
                            FileBody fileBody = new FileBody(f);
                            entity.addPart("image"+i,fileBody);
                        }else{
                            entity.addPart(params.get(i).getName(),new StringBody(params.get(i).getValue()));
                        }
                    }
                    post.setEntity(entity);
                    Log.i("SENSEsysinfo", "Upload files2. count = " + count);
                    //create response handler
                    ResponseHandler< String> handler = new BasicResponseHandler();
                    //execute and get response
                    String UploadFilesResponse = new String(client.execute(post,handler).getBytes(),HTTP.UTF_8);
                    Log.i("SENSEsysinfo", "UploadFilesResponse = " + UploadFilesResponse);
                }catch(Exception e){
                	Log.i("SENSEsysinfo", "Upload Exception = " + e);
                    e.printStackTrace();
                }
            }  
        }.start();  
	}
	
	class UploadToServerTask extends AsyncTask <Void, Void, String> {
		
		private String url, path = null;
		
		public UploadToServerTask(String url, String path){
			this.url = url;
			this.path = path;
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			Log.i("SENSEsysinfo", "UploadToServerTask: doInBackground");
		    GetServerMessage message = new GetServerMessage();
		    //String msg = message.stringQuery("http://140.113.179.233:3001/upload");
		    String msg = message.upload(this.url, this.path);
			return msg;
		}
		
		///* back to UI thread
		protected void onPostExecute(String results) {
			Log.i("SENSEsysinfo", "UploadToServerTask: onPostExecute");
			
			try {
				Log.i("SENSEsysinfo", "Response: " + results);
			} catch (Exception e) {
				Log.i("SENSEsysinfo","UploadToServerTask exception, results->" + results);
			    e.printStackTrace();
			}
			//txtResult.setText(results);
		}
    }
	
	public class GetServerMessage {
		/*
	    public String stringQuery(String url){
	        try
	        {
	            HttpClient httpclient = new DefaultHttpClient();
	            HttpGet method = new HttpGet(url);
	            HttpResponse response = httpclient.execute(method);
	            HttpEntity entity = response.getEntity();

				if (entity != null) {
	            	return EntityUtils.toString(entity,"utf-8");
	            }
	            else{
	            	Log.i("test", "stringQuery-> no string");
	                return "No string.";
	            }
	         }
	         catch(Exception e){
	             return e.toString();
	         }
	    }
	    */
	    public String upload(String url, String file_path) {
			Log.i("SENSEsysinfo", "Upload files.");
			count++;
			
			List< NameValuePair> params = new ArrayList< NameValuePair>();
			params.add(new BasicNameValuePair("imei", IMEI)); ///* put imei in req.body
            params.add(new BasicNameValuePair("file", file_path));
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            
            try{
                //setup multipart entity
                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                for(int i=0;i< params.size();i++){
                    //identify param type by Key
                    if(params.get(i).getName().equals("file")){
                        File f = new File(params.get(i).getValue());
                        FileBody fileBody = new FileBody(f);
                        entity.addPart("information",fileBody);
                    }else{
                        entity.addPart(params.get(i).getName(),new StringBody(params.get(i).getValue()));
                    }
                }
                post.setEntity(entity);
                Log.i("SENSEsysinfo", "Upload files2. count = " + count);
                //create response handler
                //ResponseHandler< String> handler = new BasicResponseHandler();
                //execute and get response
                //String UploadFilesResponse = new String(client.execute(post,handler).getBytes(),HTTP.UTF_8);
                //Log.i("SENSEsysinfo", "UploadFilesResponse = " + UploadFilesResponse);
                
                HttpResponse response = httpclient.execute(post);
	            HttpEntity res_entity = response.getEntity();

				if (res_entity != null) {
	            	return EntityUtils.toString(res_entity,"utf-8");
	            }
	            else{
	            	Log.i("SENSEsysinfo", "stringQuery-> no string");
	                return "No string.";
	            }
				
            } catch(Exception e){
            	Log.i("SENSEsysinfo", "Upload Exception = " + e);
                e.printStackTrace();
                return e.toString();
            }
        }
	}
}
	