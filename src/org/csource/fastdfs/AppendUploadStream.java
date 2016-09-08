package org.csource.fastdfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
* Upload file by stream
* @author zhouzezhong & Happy Fish / YuQing
* @version Version 1.11
*/
public class AppendUploadStream implements UploadCallback
{
	public long getRemainBytes() {
		return remainBytes;
	}

	public void setRemainBytes(long remainBytes) {
		this.remainBytes = remainBytes;
	}

	private InputStream inputStream; //input stream for reading
	private long fileSize = 0;  //size of the uploaded file
	private long remainBytes=0; 
	
	/** 
	* constructor
	* @param inputStream input stream for uploading
	* @param fileSize size of uploaded file
	*/
	public AppendUploadStream(InputStream inputStream, long fileSize)
	{
		this.inputStream = inputStream;
		this.fileSize = fileSize;
	}

	/**
	* send file content callback function, be called only once when the file uploaded
	* @param out output stream for writing file content
	* @return 0 success, return none zero(errno) if fail
	*/
	public int send(OutputStream out) throws IOException
	{
		//remainBytes代表还未上传的字节数，要从 fileSize - remainBytes开始传
		byte[] buff = new byte[256 * 1024];
		int bytes;
		inputStream.skip(fileSize - remainBytes);
		while(remainBytes > 0)
		{ 
			try
			{
				if ((bytes=inputStream.read(buff, 0, remainBytes > buff.length ? buff.length : (int)remainBytes)) < 0)
				{
					return -1;
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace(); 
				return -1;
			}
			
			out.write(buff, 0, bytes);
			remainBytes -= bytes;
		}
		System.out.println("remainBytes:"+remainBytes);
		
		
		return 0;
	}
}
