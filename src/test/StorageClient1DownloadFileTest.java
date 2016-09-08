/**
 * @desc
 * @author ma
 * @date 2015年6月2日
 * 
 */
package test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;

public class StorageClient1DownloadFileTest {
	/**
	 * 文件分段大小
	 */
	private final static int section_size =1024 * 500 * 15;
	
	private StorageClient1 client = null;
	/**
	 * 文件的属性信息，可选
	 */
	private NameValuePair[] meta_list = null;
	/**
	 * 文件扩展名
	 */
	private String file_ext_name = null;

	@Before
	public void init() throws Exception {
		ClientGlobal.init(ClientGlobal.conf_filename);
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		client = new StorageClient1(trackerServer, null);
	}


	@Test
	public void upload_file() throws Exception {
		String filepath = "G:/doc/book.zip";
		long temp = System.currentTimeMillis();
		meta_list = new NameValuePair[1];
		meta_list[0] = new NameValuePair("size", String.valueOf(new File(
				filepath).length()));
		String result = client.upload_file1(filepath, file_ext_name,
				meta_list);
	
		System.err.println(new File(filepath).length() / (1024 * 1024)
				+ "M->" + (System.currentTimeMillis() - temp) / 1000.0 + "S");
		System.err.println("fileid: " + result);
		download_file(result);
	}
	
	//类似断点下载的例子
	public void download_file(String fileid) throws Exception {
		File file = new File("c:\\"+fileid.replaceAll("/", "_"));
		System.err.println(file.length());
		long temp = System.currentTimeMillis();
		
		int result = -1;
		int count =0;
		while(result != 0 && count <50){
			//如果下载失败，继续下载，在这可以设置一定的规则,注意下面方法download_file最后一个参数为false，表示不从头开始下载
			try {
				result = client.download_file1(fileid,file.length(),0, "c:\\"
						+ fileid.replaceAll("/", "_"),false);
				count ++;
				System.err.println("count:"+count);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}
		
		System.err.println(new File("c:\\"
				+ fileid.replaceAll("/", "_")).length()
				/ (1024 * 1024)
				+ "M->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}
	
	
	

	
	//从头重新下载文件
	public void download_file5(String fileid) throws Exception {

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
	
	

	//顺序多次下载文件，每次下载都保存为一个独立的文件
	public void download_file6(String fileid) throws Exception {
		String file_ext_name = fileid.substring(fileid.lastIndexOf(".")+1);
		String prefix =  fileid.substring(0,fileid.lastIndexOf("."));
		meta_list = client.get_metadata1(fileid);
		NameValuePair mata = meta_list[0];
		System.err.println(mata.getName() + ":" + mata.getValue());
		long size = Long.parseLong(mata.getValue());
		long temp = System.currentTimeMillis();
		long buffer = section_size;
		long init = 0;
		int index = 1;
		while(init<size){
			if(size-init <=buffer){
				buffer=size-init;
			}
		int result = client.download_file1(fileid, init,
				buffer,
				"c:\\"
						+ prefix.replaceAll("/", "_").concat("-")
								.concat(String.valueOf(index)).concat(".").concat(file_ext_name));
		System.err.println(result);
		init += buffer;
		index++;
		//assertTrue("true", result == 0);
		 }

		System.err.println((System.currentTimeMillis() - temp) / 1000.0 + "S");
	}

}
