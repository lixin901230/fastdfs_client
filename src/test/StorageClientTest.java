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
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;

public class StorageClientTest {
	private StorageClient client = null;
	/**
	 * 文件的属性信息，可选
	 */
	private NameValuePair[] meta_list = null;

	@Before
	public void init() throws Exception {
		ClientGlobal.init(ClientGlobal.conf_filename);
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		client = new StorageClient(trackerServer, null);
	}

	@Test
	public void upload_file1() throws Exception {
		//上传字节数组
		byte[] file_buff = new byte[]{};
		String file_ext_name = "txt";
		String[] results = client.upload_file(file_buff, file_ext_name, meta_list);

		assertTrue("true", results.length == 2);
		System.err.println("group_name: " + results[0] + ", remote_filename: "+ results[1]);

	}
	
	@Test
	public void upload_file2() throws Exception {
		String file_ext_name = "txt";
		String local_filename = "";
		//上传文件
		String[] results = client.upload_file(local_filename, file_ext_name, meta_list);
		assertTrue("true", results.length == 2);
		System.err.println("group_name: " + results[0] + ", remote_filename: "+ results[1]);

	}

	@Test
	public void download_file1() throws Exception {
		String group_name = "";
		String remote_filename = "";
		//下载为字节数组
		byte[] file_buff = client.download_file(group_name, remote_filename);
		System.out.println((new String(file_buff)));
	}

	@Test
	public void delete_file() throws Exception {
		String group_name = "";
		String remote_filename = "";
		int result = client.delete_file(group_name, remote_filename);
		System.out.println(result);
	}
	
	@Test
	public void download_file2() throws Exception {
		String remote_filename = "";
		long temp = System.currentTimeMillis();
		//下载为文件
		int result = client.download_file("group1", remote_filename, "c:\\"
				+ remote_filename.replaceAll("/", "_"));
		assertTrue("true", result == 0);
		System.err.println(new File("c:\\"
				+ remote_filename.replaceAll("/", "_")).length()
				+ "->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}

}
