/**
 * @desc
 * @author ma
 * @date 2015年6月2日
 * 
 */
package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;

import com.taobao.taokeeper.common.constant.SystemConstant;

public class StorageClient1Test {
	private StorageClient1 client = null;
	/**
	 * 文件的属性信息，可选
	 */
	private NameValuePair[] meta_list = null;

	@Before
	public void init() throws Exception {
		ClientGlobal.init(ClientGlobal.conf_filename);
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		
		
		StorageServer storageServer = tracker.getStoreStorage(trackerServer);
  		if (storageServer == null) {
  			System.out.println("getStoreStorage fail, error code: " + tracker.getErrorCode());
  			return;
  		}
		client = new StorageClient1(trackerServer, storageServer);
	}

	@Test
	public void upload_file1() throws Exception {
		//上传字节数组
		byte[] file_buff = "中国5555555555555555555".getBytes();
		String file_ext_name = "tttttttttt";
		meta_list = new NameValuePair[2];
  		meta_list[0] = new NameValuePair("time", String.valueOf(System.currentTimeMillis()));
  		meta_list[1] = new NameValuePair("name", "文件.txt");
		String result = client.upload_file1(file_buff, file_ext_name, meta_list);
	
		System.err.println("file id : " + result);
		download_file1(result);
		download_file2(result);
	}
	
	@Test
	public void upload_file2() throws Exception {
		String file_ext_name = "";
		String local_filename = "G:/doc/config.hhhhhhhhhhhhhh";
		//上传文件
		String result = client.upload_file1(local_filename, file_ext_name, meta_list);
		System.err.println("file id : " + result);
	}
	
	public void download_file1(String fileid) throws Exception {
		//下载为字节数组
		byte[] file_buff = client.download_file1(fileid);
		System.out.println((new String(file_buff)));
	}

	@Test
	public void download_file1() throws Exception {
		String fileid = "group1/M00/00/00/rBAVulV2fIiAYDCVAAAABDid_WU437.txt";
		//下载为字节数组
		byte[] file_buff = client.download_file1(fileid);
		System.out.println((new String(file_buff)));
	}
	
	@Test
	public void getmeta_list() throws Exception{
		String fileid = "group1/M00/00/00/rBAVuVV48C-ATLeUAAAABDid_WU200.txt";
		NameValuePair[] temp_meta_list= client.get_metadata1(fileid);
		for(NameValuePair nv : temp_meta_list){
			System.err.println(nv.getName().concat("->").concat(nv.getValue()));
		}
	}

	@Test
	public void delete_file() throws Exception {
		List<String> list = SSHTester.execute("172.16.21.186",SystemConstant.portOfSSH,"root","123.com","cd /home/storage/data/00/00;ls -rS");
		while(list.size() > 1){
			try {
				String fileid = "group1/M00/00/00/";
				for(String str:list){
					int nPos = str.lastIndexOf('.');
					String temp = nPos>0?str.substring(nPos+1):"";
					if(temp.length() != 3){
						continue;
					}
					fileid = fileid.concat(str);
					
					//client = new StorageClient1(null, null);
					TrackerClient tracker = new TrackerClient();
					TrackerServer trackerServer = tracker.getConnection();
					client = new StorageClient1(trackerServer, null);
					int result = client.delete_file1(fileid);
					System.out.println(str+":"+result);
				}
				list = SSHTester.execute("172.16.21.186",SystemConstant.portOfSSH,"root","123.com","cd /home/storage/data/00/00;ls ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	@Test
	public void download_file2() throws Exception {
		String fileid = "group1/M00/00/00/rBAVulV2fIiAYDCVAAAABDid_WU437.txt";
		long temp = System.currentTimeMillis();
		//下载为文件
		int result = client.download_file1(fileid, "c:\\"
				+ fileid.replaceAll("/", "_"));
		assertTrue("true", result == 0);
		System.err.println(new File("c:\\"
				+ fileid.replaceAll("/", "_")).length()
				+ "->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}
	
	public void download_file2(String fileid) throws Exception {
		long temp = System.currentTimeMillis();
		//下载为文件
		int result = client.download_file1(fileid, "c:\\"
				+ fileid.replaceAll("/", "_"));
		assertTrue("true", result == 0);
		System.err.println(new File("c:\\"
				+ fileid.replaceAll("/", "_")).length()
				+ "->"
				+ (System.currentTimeMillis() - temp)
				/ 1000.0 + "S");
	}

}
