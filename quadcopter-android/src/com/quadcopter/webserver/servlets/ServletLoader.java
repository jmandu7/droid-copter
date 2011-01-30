package com.quadcopter.webserver.servlets;

import java.util.ArrayList;

public class ServletLoader 
{
	private ArrayList<Servlet> servlets = null;
	
	public ServletLoader()
	{
		servlets = new ArrayList<Servlet>();
	}
	
	/**
	 * This function tries to load the Servlet from an array of previously
	 * loaded servlets. If it can't find it, it tries to load the servlet
	 * based on the class name. If it can't instantiate it, then it returns
	 * null.
	 * 
	 * @param servletName
	 * @return
	 */
	public Servlet loadServlet(String servletName)
	{
		Servlet ret = searchLoadedServlets(servletName);
		if (ret==null)
		{
			ret = instanceOfServlet(servletName);
			if (ret != null)
			{
				servlets.add(ret);
			}
		}
		return ret;
	}
	
	private Servlet instanceOfServlet(String servletName)
	{
		Servlet ret = null;
		
		try
		{
			ClassLoader classLoader = ServletLoader.class.getClassLoader();
			
			Class<?> servletClass = classLoader.loadClass(servletName);
			
			ret = (Servlet) servletClass.newInstance();
			
		} catch (Exception e)
		{
			//There was an error, so assume that 
			//we couldn't load the class
			ret = null;
		}
	    return ret;
	}
	
	private Servlet searchLoadedServlets(String servletName)
	{
		Servlet ret = null;
		try
		{
			for (Object o: servlets)
			{
				if (o.getClass().getName().equals(servletName)) 
				{
					ret = (Servlet) o;
				}
			}
		} catch (Exception e)
		{
			//There was an error, so assume that 
			//we couldn't find the servlet in the array
			ret = null;
		}
		return ret;
	}
}
