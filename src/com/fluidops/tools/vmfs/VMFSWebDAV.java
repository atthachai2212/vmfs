/*
 * VMFSWebDAV.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.fluidops.base.Version;
import com.fluidops.tools.vmfs.VMFSDriver.FileMetaInfo;
import com.fluidops.util.StringUtil;
import com.fluidops.util.logging.Debug;

/**
 * VMFS to HTTP/WebDAV Server Brigde.
 * 
 * @author Uli
 */
public class VMFSWebDAV implements ResourceFactory, Initable
{
	/**
	 * The VMFS driver instance to use.
	 */
	static VMFSDriver vmfs;

	/**
	 * Represents a WebDAV file resource.
	 * @author Uli
	 */
	public class WebDAVFile implements Resource, GetableResource, PropFindableResource
	{
		String host, path;
		FileMetaInfo fr;
		
		WebDAVFile( String host, String path, FileMetaInfo fr )
		{
			this.host = host; this.path = path;
			this.fr = fr;
		}

		@Override
		public Object authenticate(String arg0, String arg1)
		{
			return null;
		}

		@Override
		public boolean authorise(Request arg0, Method arg1, Auth arg2)
		{
			return true;
		}

		@Override
		public String checkRedirect(Request arg0)
		{
			return null;
		}

		@Override
		public Date getModifiedDate()
		{			
			return new Date( 1000L * fr.fmr.timeStamp1__0x2c );
		}

		@Override
		public String getName()
		{
			return new java.io.File( path ).getName();
		}

		@Override
		public String getRealm()
		{
			return null;
		}

		@Override
		public String getUniqueId()
		{
			return null;
		}

		@Override
		public int compareTo(Resource o)
		{
			if ( o instanceof WebDAVFile )
				return this.path.compareTo( ((WebDAVFile)o).path );
			
			return -1;
		}

		@Override
		public Long getContentLength()
		{
			return fr.fmr.size;
		}

		@Override
		public String getContentType(String arg0)
		{
			return "application/octet-stream";
		}

		@Override
		public Long getMaxAgeSeconds()
		{
			return 0L;
		}

		@Override
		public void sendContent(OutputStream out, Range range,
				Map<String, String> args) throws IOException
		{
			long s = 0, e = fr.fmr.size - 1;
			if ( range!=null )
			{
				s = range.getStart();
				e = range.getFinish();
			}
			
			IOAccess io = vmfs.openFile( intern(path) );
			io.setPosition( s );
			long todo = e - s + 1;
			byte[] buffer = new byte[ 16384 ];
			while ( todo>0 )
			{
				int readNow = (int)Math.min(buffer.length, todo);
				int got = io.read( buffer ,0 ,readNow );
				if ( got<=0 ) break;
				out.write( buffer, 0, got );
				
				todo -= got;
			}
			io.close();
		}

		@Override
		public Date getCreateDate()
		{
			return getModifiedDate();
		}
		
		public VMFSDriver.FileMetaInfo getVmfsFileMetaInfo()
		{
			return fr;
		}
	}
	
	/**
	 * Represents a WebDAV folder resource.
	 * @author Uli
	 */
	public class WebDAVFolder extends WebDAVFile implements CollectionResource
	{
		WebDAVFolder( String host, String path, FileMetaInfo fr )
		{
			super(host, path, fr);
		}
		
		@Override
		public Long getContentLength()
		{
			return null;
		}
		
		@Override
		public String getContentType(String arg0)
		{
			return "text/html";
		}

		@Override
		public Resource child(String name)
		{
			String p = path;
			if ( !p.endsWith("/") ) p += "/";
			p += name;
			return getResource( host, p );
		}

		@Override
		public List<? extends Resource> getChildren()
		{
			List<Resource> r = new ArrayList<Resource>();
			try
			{
				List<FileMetaInfo> res = vmfs.dir( intern(path) );
				for ( FileMetaInfo fmi : res )
				{
					r.add( child( fmi.fr.name__128 ) );
				}
			}
			catch ( Exception ex )
			{
				ex.printStackTrace();
			}

			return r;
		}
		
		@Override
		public void sendContent(OutputStream out, Range arg1,
				Map<String, String> arg2) throws IOException
		{
			PrintStream ps = new PrintStream( out );
			ps.println("<html><head><title>"+intern(path)+"</title></head>");
			ps.println("<body>");
			ps.println("<h1>Volume Information</h1><pre>");
			try
			{
				VMFSTools.showVMFSInfo(ps, vmfs);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			ps.println("</pre>");
			ps.println("<h1>Folder "+intern(path)+"</h1>");
			
			ps.println("<table border='0' cellpadding=5><tr><th>Name</th><th>Size</th><th>Date</th></tr>");
//			if ( intern(path).length()>0 && !intern(path).equals("/") )
//				ps.println("<li><a href='"+new java.io.File(intern(path)).getParent()+"'>(parent)</a></li>");
			
			List<? extends Resource> c = getChildren();
			for ( Resource r : c )
			{
				ps.println("<tr>");
				WebDAVFile f = (WebDAVFile)r;
				VMFSDriver.FileMetaInfo fmi = f.getVmfsFileMetaInfo();
				switch (fmi.fmr.type )
				{
					case VMFSDriver.TYPE_META:
					case VMFSDriver.TYPE_FILE:
						ps.println("<td><a href='"+ f.path + "'>" + r.getName() + "</a></td>");
						ps.println("<td align='right'>"+StringUtil.displaySizeInBytes(f.getContentLength())+"</td>");
						ps.println("<td align='right'>"+r.getModifiedDate()+"</td>");
						break;
					case VMFSDriver.TYPE_FOLDER:
						ps.println("<td><a href='"+ f.path + "'>" + r.getName() + "</a></td>");
						ps.println("<td align='right'>(dir)</td>");
						ps.println("<td align='right'>"+r.getModifiedDate()+"</td>");
						break;
					case VMFSDriver.TYPE_SYMLINK:
						ps.println("<td>" + r.getName() + "</td>");
						ps.println("<td align='right'>(symlink)</td>");
						ps.println("<td align='right'>"+r.getModifiedDate()+"</td>");
						String lnk = vmfs.getSymLink(fmi.fr);
						ps.println("<td>&rarr; <a href='"+extern("/"+lnk)+"'>"+ lnk +"</a></td>");
						break;
					case VMFSDriver.TYPE_RDM:
						ps.println("<td>" + r.getName() + "</td>");
						ps.println("<td align='right'>(rdm "+StringUtil.displaySizeInBytes(fmi.fmr.size) +")</td>");
						ps.println("<td align='right'>"+r.getModifiedDate()+"</td>");
						ps.println("<td>&rarr; Type=["+ fmi.rdm.lunType__28+"] UUID=["+fmi.rdm.lunUuid__17+"]");
						break;
					default:
						ps.println("<td>" + r.getName() + "</td>");
						ps.println("<td align='right'>(unknown:"+fmi.fmr.type+")</td>");
						ps.println("<td align='right'>"+r.getModifiedDate()+"</td>");
				}
				ps.println("</tr>");
			}
			ps.println("</table>");

			ps.println("<br/>VMFSTools (c) by <a href='http://www.fluidops.com'>fluid Operations</a> (v"+
					Version.getVersion()+" r"+Version.getRevision()+" / " +Version.getBuildDate() + ")");

			ps.println("</body></html>");
			ps.flush();
		}
	}
	
	static String prefix = "/vmfs";
	
	public static String getPathPrefix()
	{
		return prefix;
	}
	
	public static void setPathPrefix( String prefix )
	{
		VMFSWebDAV.prefix = prefix;
	}
	
	String intern( String path )
	{
		String r = path.substring( prefix.length() );
		return r;
	}
	
	String extern( String path )
	{
		return prefix + path;
	}

	@Override
	public Resource getResource(String host, String url)
	{
		try
		{		
			String path = intern(url);
			
			if ( path.equals("/") || path.length()==0 ) path = ".";

			FileMetaInfo fr = vmfs.getMetaInfoForFile( path );
			if ( fr==null ) return null;
			if ( fr.fr.isFolder() )
				return new WebDAVFolder( host, url, fr );
			else
				return new WebDAVFile( host, url, fr );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String getSupportedLevels()
	{
		return "1"; // "1,2"
	}

	@Override
	public void init(ApplicationConfig config, HttpManager manager)
	{
		// nothing to do
	}

	@Override
	public void destroy(HttpManager manager)
	{
		// nothing to do
	}
	
	public static void runWebDAVServer( VMFSDriver vd, String host, int port ) throws Exception
	{
		vmfs = vd;
		
    	// Enable Log4J if debug is set
    	if ( Debug.debug )
    		BasicConfigurator.configure();
    	
    	System.out.println( "*** Serving WebDAV/HTTP at http://"+host+":"+port+VMFSWebDAV.getPathPrefix() );

    	// Create a new HTTP server
        Server server = new Server();
        
        // Add a connector for host:port
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost(host);
        connector.setPort(port);
        connector.setReuseAddress(false);
        connector.setSoLingerTime(0);
        server.addConnector(connector);
        
        // Add the servlet mapping
        ServletHolder holder = new ServletHolder( com.bradmcevoy.http.MiltonServlet.class );
        holder.setDisplayName( "MiltonServlet" );
        holder.setInitParameter( "resource.factory.class", VMFSWebDAV.class.getCanonicalName() );
        ServletHandler sh = new ServletHandler();
        sh.addServletWithMapping( holder, VMFSWebDAV.getPathPrefix()+"/*" );
        server.addHandler(sh);
//        XmlConfiguration configuration = new XmlConfiguration(new File("etc/jetty.xml").toURI().toURL());
//        configuration.configure(server);
        server.start();
        server.join();
	}
}
