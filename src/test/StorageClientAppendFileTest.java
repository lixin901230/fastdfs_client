/**
 * @desc 实现一个大文件客户端分段顺序上传
 * @author ma
 * @date 2015年6月2日
 * 
 */
package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;

public class StorageClientAppendFileTest {
	/**
	 * 文件分段大小
	 */
	private final static int section_size =1024 * 500 * 15;
	
	private String  filepath = "G:/doc/test1.zip";
	
	private StorageClient client = null;

	@Before
	public void init() throws Exception {
		ClientGlobal.init(ClientGlobal.conf_filename);
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		client = new StorageClient(trackerServer, null);
		System.out.println("section_size is "+ section_size/(1024*1024*1.0)+"M");
	}
	
	//上传一个0字节，获取一个fileid
	@Test
	public void upload_file5_1() throws Exception {
		long start = System.currentTimeMillis();
		String[] results = client.upload_appender_file(new byte[]{},"zip",null);
		assertTrue("true", results.length == 2);
		
		System.err.println("group_name: " + results[0] + ", remote_filename: "+ results[1]);
		System.err.println("upload zero byte cost time "+(System.currentTimeMillis()-start)/1000.0);
		
		String group_name = results[0];
		String remote_filename = results[1];
		upload_file(group_name,remote_filename);
		System.err.println("uoload cost time "+(System.currentTimeMillis()-start)/1000.0);
		
		start = System.currentTimeMillis();
		download_file(group_name,remote_filename);
		System.err.println("download cost time "+(System.currentTimeMillis()-start)/1000.0);
	}
	
	// 将文件按照单位大小分次追加到指定的fileid
	public long upload_file(String group_name, String remote_filename)
			throws Exception {
		long length = new File(filepath).length();
		int size = 0;
		byte[] buff = new byte[section_size];
		InputStream is = new FileInputStream(filepath);
		long start = System.currentTimeMillis();
		while (size < length) {
			long temp = System.currentTimeMillis();
			int readcount = is.read(buff, 0,
					(length - size) < buff.length ? (int) (length - size)
							: buff.length);
			System.out.println("readcount:" + readcount);
			// 3次尝试上传都失败则不上传该文件，并记录已上传的大小，下次上传可以skip已上传大小后，再上传
			int count = 0;
			while (true) {
				int result = -1;
				try {
					byte[] newbuffer;
					if (readcount < buff.length) {
						newbuffer = new byte[readcount];
						System.arraycopy(buff, 0, newbuffer, 0, readcount);
						result = client.append_file(group_name, remote_filename, newbuffer);
					} else {
						result = client.append_file(group_name, remote_filename, buff);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				count++;
				if (result == 0) {
					break;
				}
				if (count >= 3) {
					return size;
				}
			}
			size += readcount;
			System.err.println("upload " + readcount + " byte cost time "
					+ (System.currentTimeMillis() - temp) / 1000.0);
			System.err.println("upload " + size + " byte cost time "
					+ (System.currentTimeMillis() - start) / 1000.0);
		}
		return length;
	}

	public void download_file(String group_name, String remote_filename) throws Exception {
		long temp = System.currentTimeMillis();
		int result = client.download_file("group1", remote_filename, 
				"c:\\"+ remote_filename.replaceAll("/", "_"));
		assertTrue("true", result == 0);
		System.err.println(new File("c:\\"
				+ remote_filename.replaceAll("/", "_")).length()
				/ (1024 * 1024)
				+ "M->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}
	
	
	@Test
	public void upload_file5_3() throws Exception {
		// 10M->0.902S
		for(int i=3 ;i <=11;i++){
			String filepath = "C:/temp/M00_00_00_rBAVuVVtY36AIBeHAKHlbltsPPE834.zip-"+i;
			long temp = System.currentTimeMillis();
			int result = client.append_file("group1", "M00/00/00/rBAVuVVumuOEBxOIAAAAANKW2uQ673.zip",filepath);
			assertTrue("true", result == 0);
			System.err.println(new File(filepath).length() / (1024 * 1024) + "M->"
					+ (System.currentTimeMillis() - temp) / 1000.0 + "S");
		}
		
	}
}
