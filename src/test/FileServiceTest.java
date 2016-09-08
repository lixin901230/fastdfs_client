/**
 * @desc
 * @author ma
 * @date 2015年6月15日
 * 
 */
package test;

import org.csource.FileService;
import org.csource.UploadResultInfo;
import org.junit.Test;


public class FileServiceTest  {
	
	private static String filepath ="g:/upload/zookeeper.doc";
	
	@Test
	public void upload_file(){
		for(int i =0;i<1;i++){
			UploadResultInfo info = FileService.getInstance().upload_file(filepath, null);
			System.out.println(info);
		}
		
	}
	
	@Test
	public void upload_file2(){
		String fileid = "group1/M00/00/00/rBAVuVV-gY2Eau_8AAAAAAAAAAA909.zip";
		FileService.getInstance().upload_file(filepath, fileid);
	}
}


