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
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;

public class StorageClient1AppendFileTest {
	/**
	 * 文件分段大小
	 */
	private final static int section_size =1024 * 500 * 50;
	
	private String  filepath = "G:/doc/book.zip";
	
	private StorageClient1 client = null;

	@Before
	public void init() throws Exception {
		ClientGlobal.init(ClientGlobal.conf_filename);
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		client = new StorageClient1(trackerServer, null);
		System.err.println("section_size is "+ section_size/(1024*1024*1.0)+"M");
	}
	
	
	@Test
	public void upload_file5_1() throws Exception {
		long start = System.currentTimeMillis();
		// 上传一个0字节，获取一个fileid
		String result = client.upload_appender_file1(new byte[] {}, "zip", null);

		System.err.println("fileid: " + result);
		System.err.println("upload zero byte cost time "+ (System.currentTimeMillis() - start) / 1000.0);

		long size = 0l;
		// long size = upload_file(result);
		// System.err.println("uoloaded size "+size);
		// System.err.println("uoload cost time "+(System.currentTimeMillis()-start)/1000.0);

		long temp = System.currentTimeMillis();
		while ((System.currentTimeMillis() - temp) / 1000 < 60) {
			//因异常中断服务端会抹去追加的部分，需等待服务端抹去后再上传
			while (!compare(result, size)) {
				Thread.sleep(1000);
			}
			size = upload_file5_3(result, size);
			Thread.sleep(1000 * 2);
			System.err.println("upload_file5_3 size " + size);
		}
		// 上传剩余文件
		// upload_file5_3(result,size);

		start = System.currentTimeMillis();
		download_file(result);
		System.err.println("download cost time "+ (System.currentTimeMillis() - start) / 1000.0);
	}
	
	// 将文件按照单位大小分次追加到指定的fileid
	@SuppressWarnings("resource")
	public long upload_file(String fileid) throws Exception {
		long length = new File(filepath).length();
		int size = 0;
		byte[] buff = new byte[section_size];
		InputStream is = new FileInputStream(filepath);
		System.err.println("file size: "+length);
		long start = System.currentTimeMillis();
		while (size < length) {
			long temp = System.currentTimeMillis();
			int readcount = is.read(buff, 0,
					(length - size) < buff.length ? (int) (length - size)
							: buff.length);
			System.err.println("readcount:" + readcount);
			// 3次尝试上传都失败则不上传该文件，并记录已上传的大小，下次上传可以skip已上传大小后，再上传
			int count = 0;
			while (true) {
				int result = -1;
				try {
					byte[] newbuffer;
					if (readcount < buff.length) {
						newbuffer = new byte[readcount];
						System.arraycopy(buff, 0, newbuffer, 0, readcount);
						result = client.append_file1(fileid, newbuffer);
					} else {
						result = client.append_file1(fileid, buff);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				count++;
				if (result == 0) {
					break;
				}
				if (count >= 1) {
					return size;
				}
			}
			size += readcount;
			System.err.println("upload " + readcount + " byte cost time "
					+ (System.currentTimeMillis() - temp) / 1000.0);
			System.err.println("upload " + size + " byte cost time "
					+ (System.currentTimeMillis() - start) / 1000.0);
			FileInfo f = client.get_file_info1(fileid);
			System.err.println("服务端文件大小:"+f.getFileSize()+","+f.getSourceIpAddr());
		}
		return length;
	}

	public void download_file(String fileid) throws Exception {
		long temp = System.currentTimeMillis();
		int result = client.download_file1(fileid, "c:\\"
				+ fileid.replaceAll("/", "_"));
		assertTrue("true", result == 0);
		System.err.println(new File("c:\\"
				+ fileid.replaceAll("/", "_")).length()
				/ (1024 * 1024)
				+ "M->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}
	
	/**
	 * 
	 * @param fileid 追加文件的文件id
	 * @param skipsize 已上传的大小
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public long upload_file5_3(String fileid,long skipsize) throws Exception {
		long length = new File(filepath).length();
		if(skipsize >=  length){
			return length;
		}
		int size = 0;
		byte[] buff = new byte[section_size];
		InputStream is = new FileInputStream(filepath);
		is.skip(skipsize);
		System.err.println("file size: "+length);
		long start = System.currentTimeMillis();
		while (skipsize < length) {
			int readcount = is.read(buff, 0,
					(length - skipsize) < buff.length ? (int) (length - skipsize)
							: buff.length);
			int count = 0;
			while (true) {
				int result = -1;
				try {
					byte[] newbuffer;
					if (readcount < buff.length) {
						newbuffer = new byte[readcount];
						System.arraycopy(buff, 0, newbuffer, 0, readcount);
						result = client.append_file1(fileid, newbuffer);
					} else {
						result = client.append_file1(fileid, buff);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				count++;
				if (result == 0) {
					break;
				}
				if (count >= 1) {
					return skipsize;
				}
			}
			size += readcount;
			skipsize += readcount;

			System.err.println("upload " + size + " byte cost time "
					+ (System.currentTimeMillis() - start) / 1000.0+",upload total "+skipsize);
		}
		return length;
	}
	
	/**
	 * 比较本地和服务器的文件大小是否相等，如果不相等则代表服务端在异常中断后还未恢复到异常中断之前
	 * @param fileid
	 * @param size
	 * @return
	 */
	public boolean compare(String fileid,long size){
		try {
			FileInfo f = client.get_file_info1(fileid);
			System.err.println("服务端文件大小为"+f.getFileSize()+",本地记录已上传文件大小为:"+size);
			return f.getFileSize() == size;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	
}
