package com.quadcopter.background.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.quadcopter.background.BackgroundService;
import com.quadcopter.webserver.servlets.Servlet;
import com.quadcopter.webserver.servlets.ServletLoader;

public class WebServerThread extends Thread implements HttpRequestHandler
{
	private static final String TAG = "WebServerThread";
	
	private static final String SERVLET_PACKAGE = "com.quadcopter.webserver.servlets";

	private int mPort;

	Context mContext;
	
	private ServerSocket mServerSocket;

	private SharedPreferences mSharedPreferences;

	private SQLiteDatabase mCookiesDatabase;

	/* How long we allow session cookies to last. */
	private static final int COOKIE_EXPIRY_SECONDS = 3600;
	
	private byte[] webCameraImg = null;
	
	private ServletLoader servletLoader = null;
	
    private BasicHttpProcessor httpproc = null;
    private BasicHttpContext httpContext = null;
    private HttpService httpService = null;
    private HttpRequestHandlerRegistry registry = null;
    private final HttpParams params; 
    
	
	public void setWebCamImg(byte[] img)
	{
		webCameraImg = img;
	}
	
	/* Returns port we're using */
	public int getPort() 
	{
		return mPort;
	}

	/* Start the webserver on specified port */
	public WebServerThread(Context context, SharedPreferences sharedPreferences,
			SQLiteDatabase cookiesDatabase, int port) throws IOException 
	{
		mContext = context;
		mPort = port;
		mServerSocket = new ServerSocket(mPort);
		mServerSocket.setReuseAddress(true);
		mSharedPreferences = sharedPreferences;
		//mCookiesDatabase = cookiesDatabase;
		//deleteOldCookies();
		
		//Make sure we have an instance of servletLoader 
		//ServletLoader will load an instance of a servlet
		//We have a global variable because, it will keep
		//previously loaded servlets in memory.
		servletLoader = new ServletLoader();
		//some servlets need to be loaded as soon as we start
		//the web server. So . . . we will load them here.
		Servlet servlet;
		servlet = servletLoader.loadServlet(SERVLET_PACKAGE + "." + "CounterServlet");
		servlet.setupServlet(context);
//		servlet = servletLoader.loadServlet(SERVLET_PACKAGE + "." + "GetMessageServlet");
//		servlet.setupServlet(context);
//		servlet = servletLoader.loadServlet(SERVLET_PACKAGE + "." + "SendMessageServlet");
//		servlet.setupServlet(context);
		servlet = servletLoader.loadServlet(SERVLET_PACKAGE + "." + "ControlReceiverServlet");
		servlet.setupServlet(context);
		
		
		//TODO - may delete this stuff
		//use this to handle the request
		httpproc = new BasicHttpProcessor();
        httpContext = new BasicHttpContext();
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());
        httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(),new DefaultHttpResponseFactory());
        registry = new HttpRequestHandlerRegistry();
        registry.register("*", this);   
        httpService.setHandlerResolver(registry);
        this.params = new BasicHttpParams();
        this.params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        httpService.setParams(params);
	}

	@Override
	public void run() 
	{
		super.run();
		
		//This crazy loop with a thread inside is because mServerSocket.accept()
		//pauses until a request is made. Once a request is made, it creates a 
		//thread to handle the request and then repeats the loop. The whole web
		//server should be run within a thread too. That is dealt with in the
		//WebServerService.java class
		Log.i(TAG, "Running main webserver thread");
		while (!interrupted()) {
			try {
				final Socket socket = mServerSocket.accept();
				Log.d(TAG, "Socket accepted");
                
				final DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
				serverConnection.bind(socket, new BasicHttpParams());   
				
				Thread t = new Thread(){
					@Override
					public void run() {
						
						try {
							while (!Thread.interrupted() && serverConnection.isOpen()) {
								httpService.handleRequest(serverConnection, httpContext);
			                }
						} catch (IOException e) {
		                    e.printStackTrace();
		                } catch (HttpException e) {
		                	e.printStackTrace();
		                } finally
		                {
		                	try {
								serverConnection.shutdown();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                }
						//super.run();
					}	
				};
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				Log.e(TAG, "Problem accepting socket " + e.toString());
			}
		}
		try {
			mServerSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "Problem closing socket " + e.toString());
		}
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		try
		{
			RequestLine requestLine = request.getRequestLine();
			
			/* First make sure user is logged in if that is required. */
			boolean loggedIn = true;
			//TODO set to false to require login
			if (mSharedPreferences.getBoolean(BackgroundService.PREFS_REQUIRE_LOGIN, true)) {
				/* Does the user have a valid cookie? */
				Header cookiesHeader = request.getFirstHeader("Cookie");
				if (cookiesHeader != null) {
					String cookies = cookiesHeader.getValue();
					String cookie = cookies.substring(cookies.indexOf("id=")
							+ "id=".length());
					loggedIn = isValidCookie(cookie);
				}
			} else {
				loggedIn = true;
			}

			if (!loggedIn) {
				/* Could be the result of the login form. */
				if (requestLine.getUri().equals("/login")) {
					handleLoginRequest(request, response, requestLine);
				} else {
					sendHTMLPage("login/form.html", response);
				}
			} else if (requestLine.getUri().equals("/")) {
				Log.i(TAG, "Default Page");
				sendHTMLPage("default.html", response);
			} else if (requestLine.getUri().startsWith("/webcam.jpg")) {
				Log.i(TAG, "Sending WebCam Image");
				sendWebCamImage(response);
			} else {
				doSomeSweetAction(request, response, requestLine);
			}
		} catch (IOException e) {
			Log.e(TAG, "Problem with socket " + e.toString());
		} catch (HttpException e) {
			Log.e(TAG, "Problemw with HTTP server " + e.toString());
		}
	}
	
	/**
	 * This function tries to load a servlet. If it can't find a servlet
	 * then it tries to load a page from the root directory. If it still
	 * can't find anything then it sends a "page not found" page
	 * 
	 * @param serverConnection
	 * @param request
	 * @param requestLine
	 * @throws HttpException
	 * @throws IOException
	 */
	private void doSomeSweetAction(
				HttpRequest request,
				HttpResponse response,
				RequestLine requestLine) throws HttpException, IOException
	{
		//The string that is to the left of the question mark
		//and without the leading slash is the page name
		String uri = requestLine.getUri();
		//remove everything to the right of the question mark
		String pageName = uri.contains("?")?uri.split("\\?")[0]:uri;
		//remove everything to the right of the pound
		pageName = pageName.contains("#")?pageName.split("#")[0]:pageName;
		//replace the leading slash
		pageName = pageName.charAt(0)=='/'?pageName.replaceFirst("/", ""):pageName;
		
		//Attempt to load page as a servlet. If we can't 
		//load it as a servlet then we will attempt to load
		//the file within our root html directory
		Servlet servlet = servletLoader.loadServlet(SERVLET_PACKAGE + "." + pageName);
		if (servlet==null)
		{
			
			//data = readFileFromHomeDirectory(pageName);
			File f = new File(Environment.getExternalStorageDirectory()+"/QuadCopter/" + pageName);
			if(f.exists())
			{
				FileInputStream fis = new FileInputStream(f);
				String contentType = URLConnection.guessContentTypeFromStream(fis);
				response.setEntity(new InputStreamEntity(fis, f.length()));
				response.setHeader("Content-Type", contentType);
				Log.i(TAG, "Sending page - " + pageName);
			} else
			{
				//if data is null then we couldn't load from servlet or file. So
				//we send the page not found.
				Log.i(TAG, "No action for " + requestLine.getUri());
				sendNotFound(response);
			}
		} else 
		{
			//run servlet
			Log.i(TAG, "Runing servlet - " + SERVLET_PACKAGE + "." + pageName);
			servlet.runServlet(request, response);
		}
	}

	private void handleLoginRequest(
			HttpRequest request,
			HttpResponse response,
			RequestLine requestLine) throws HttpException, IOException 
	{

		HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
		InputStream s = entity.getContent();
		StringBuffer form = new StringBuffer();
		int b;
		while ((b=s.read())!=-1)
			form.append((char)b);
		
		String password = form.substring(form.indexOf("=") + 1);
		if (password.equals(mSharedPreferences.getString(
				BackgroundService.PREFS_PASSWORD, "Pa%24%24word"))) {
			response.setStatusLine(new HttpVersion(1, 1), 302, "Found");
			
			response.addHeader("Location", "/");
			response.addHeader("Set-Cookie", "id=" + createCookie());
			
			sendHTMLPage("login/success.html", response);
		} else {
			response.setStatusLine(new HttpVersion(1, 1), 401, "Unauthorized");
			
			sendHTMLPage("login/fail.html", response);
		}
	}

	private String createCookie() 
	{
		Random r = new Random();
		String value = Long.toString(Math.abs(r.nextLong()), 36);
		ContentValues values = new ContentValues();
		values.put("name", "id");
		values.put("value", value);
		values.put("expiry", (int) System.currentTimeMillis() / 1000
				+ COOKIE_EXPIRY_SECONDS);
		mCookiesDatabase.insert("cookies", "name", values);
		return value;
	}

	private boolean isValidCookie(String cookie) 
	{
		Cursor cursor = mCookiesDatabase.query("cookies",
				new String[] { "value" },
				"name = ? and value = ? and expiry > ?", new String[] { "id",
						cookie, "" + (int) System.currentTimeMillis() / 1000 },
				null, null, null);
		boolean isValid = cursor.getCount() > 0;
		cursor.close();
		return isValid;
	}

	private void deleteOldCookies() 
	{
		mCookiesDatabase.delete("cookies", "expiry < ?", new String[] { ""
				+ (int) System.currentTimeMillis() / 1000 });
	}

	private void sendNotFound(HttpResponse response)
			throws UnsupportedEncodingException, HttpException, IOException 
	{
		//HttpResponse response
		response.setStatusLine(new HttpVersion(1, 1),404, "NOT FOUND");
		response.setEntity(new StringEntity("NOT FOUND"));
		response.addHeader("Content-Type", "text/html");
	}
	
	private void sendHTMLPage(String page, 
			Header header,
			HttpResponse response)
			throws UnsupportedEncodingException, HttpException, IOException 
	{
		response.setStatusLine(new HttpVersion(1, 1), 200,"OK");
		String body = readFileFromHomeDirectory(page);
		response.setEntity(new StringEntity(body));
		response.addHeader("Content-Type", "text/html");
	}
	
	private void sendHTMLPage(String page, HttpResponse response)
			throws UnsupportedEncodingException, HttpException, IOException 
	{
		String body = readFileFromHomeDirectory(page);
		response.setEntity(new StringEntity(body));
		response.addHeader("Content-Type", "text/html");
	}
	
	private void sendWebCamImage(HttpResponse response) throws IOException, HttpException 
	{
		 response.setStatusLine(new HttpVersion(1, 1), 200,"OK");
		 addImageEntity(webCameraImg, response);
	}
	
	private void addImageEntity(byte[] img, HttpResponse response) throws IOException 
	{
		String contentType = "application/octet-stream";
		contentType = "image/jpg";
		if (img!=null)
		{
			response.addHeader("Content-Type", contentType);
			//response.addHeader("Content-Length", "" + img.length);
			response.setEntity(new ByteArrayEntity(img));
		}
	}

	public static String readFileFromHomeDirectory(String strFileName)
	{
		String ret = "";
		try{
			
			File f = new File(Environment.getExternalStorageDirectory()+"/QuadCopter/" + strFileName);
			FileInputStream fileIS = new FileInputStream(f);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			String readString = new String();
			while((readString = buf.readLine())!= null){
				ret += readString + "\n";
			}
			fileIS.close();
		} catch (Exception e) {
			ret = null;
		} 
		return ret;
	}
}
