/**
 * @desc 文件服务类:文件的上传、下载采取短链接方式，每次上传、下载之前需要获取、是否链接资源；在分片传输过程中保证使用同一个连接来传输所有分片数据
 * @author ma
 * @date 2015年6月1日
 * 
 */
package org.csource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

public class FileService {
	
	private static final Logger logger = Logger.getLogger(FileService.class);
	
	/**
	 * 文件分段大小
	 */
	private static FileService instance = null;
//	private StorageClient1 client = null;
	private TrackerClient tracker = null;
//	private	TrackerServer trackerServer = null;
//	private StorageServer storageServer = null;

	
	private FileService() throws Exception {

		ClientGlobal.init(ClientGlobal.conf_filename);
		tracker = new TrackerClient();
		
	}

	public static FileService getInstance() {
		try {
			if (instance == null) {
				synchronized (FileService.class) {
					instance = new FileService();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public void upload_file(String local_filename){
		//从文件名和fileid关系映射服务器获取fileid
		String fileid = null;
		UploadResultInfo uploadresultinfo = upload_file(local_filename, fileid);
		//将UploadResultInfo和fileid存储到文件名和fileid关系映射服务器
	}
	
	
	/**
	 * 如果fileid不为空则代表是续传
	 * @param local_filename
	 * @param fileid
	 * @return
	 */
	public UploadResultInfo upload_file(String local_filename, String fileid) {
		StorageClient1 client = null;
		try {
			client = assignResourse(fileid);
		} catch (Exception e) {
			return new UploadResultInfo(fileid,local_filename, e.getMessage());
		}
		
		try{
			// 如果file没有值则代表是新文件上传不是续传
			try {
				if (fileid == null || fileid.trim().length() == 0) {
					String ext_name = fetchFileExtName(local_filename);
					fileid = client.upload_appender_file1(new byte[] {}, ext_name,
							null);
				}
			} catch (Exception e) {
				return new UploadResultInfo(fileid,local_filename, "上传文件失败");
			} 

			// ------------开始按照文件切片追加文件----------------------
			long size = 0;
			try {
				File file = new File(local_filename);
				long length = file.length();
				logger.debug("FileService StorageServer:"
						+ client.getStorageServer() + ",TrackerServer:"
						+ client.getTrackerServer());
				FileInfo fileInfo = fetchFileInfo(client,fileid);
	 			size = fileInfo.getFileSize();
	 			
				logger.debug("FileService StorageServer:"
						+ client.getStorageServer() + ",TrackerServer:"
						+ client.getTrackerServer());
				// 客户端记录已上成功上传到服务端的记录
				size = upload_file(client,fileid, size, length, new FileInputStream(file));
				//只有当完全上传成功才返回一个完整的UploadResultInfo对象
				if(size == length){
					UploadResultInfo uploadresultinfo = new UploadResultInfo(fileid,true,local_filename, "上传文件成功",size);
					uploadresultinfo.setUrl(ProtoCommon.HTTP.concat(fileInfo.getSourceIpAddr()).concat("/").concat(fileid));
					return uploadresultinfo;
				}
				return new UploadResultInfo(fileid,local_filename, "追加文件失败1",size);				
			} catch (FileNotFoundException e) {
				return new UploadResultInfo(fileid,local_filename, "本地文件未找到，追加失败",size);
			} catch (Exception e) {
				return new UploadResultInfo(fileid,local_filename, "追加文件失败2",size);
			}
			
		}finally {
			releaseResourse(client);
		}
	}
	
	/**
	 * 
	 * @param fileid   远程文件标识用于追加
	 * @param skipsize 已成功上传的文件大小
	 * @param length   完整文件大小
	 * @param is       完整文件流
	 * @return
	 * @throws Exception
	 */
	public long upload_file(StorageClient1 client,String fileid, long skipsize, long length,
			InputStream is) throws Exception {
		if (skipsize >= length) {
			return length;
		}
		byte[] buff = new byte[ClientGlobal.section_size];
		is.skip(skipsize);
		while (skipsize < length) {
			int readcount = is.read(buff, 0,
							(length - skipsize) < buff.length ? (int) (length - skipsize)
									: buff.length);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (result != 0) {
				return skipsize;
			}
			skipsize += readcount;
			logger.debug(fileid+"->skipsize:"+skipsize);
		}
		return length;
	}
	
	/**
	 * 获取文件扩张名
	 * @param filename
	 * @return
	 */
	private String fetchFileExtName(String filename){
		int nPos = filename.lastIndexOf('.');
		return  nPos>0?filename.substring(nPos+1):"";
	}
	

	/**
	 * download file from storage server
	 * 
	 * @param group_name
	 *            the group name of storage server
	 * @param remote_filename
	 *            filename on storage server
	 * @return file content/buff, return null if fail
	 */
	public byte[] download_file(StorageClient1 client,String fileid)
			throws IOException, MyException {
		final long file_offset = 0;
		final long download_bytes = 0;

		return client.download_file1(fileid, file_offset,
				download_bytes);
	}
	
	/**
	 * 
	 * @param fileid
	 * @return
	 * @throws IOException
	 * @throws MyException
	 */
	public int delete_file(String fileid)
			throws IOException, MyException {
		StorageClient1 client = null;
		try {
			client = assignResourse(fileid);
			return delete_file(client,fileid);
		} catch (MyException e) {
			return 99;
		}finally{
			releaseResourse(client);
		}
		
	}
	

	/**
	 * 
	 * @param client
	 * @param fileid
	 * @return
	 * @throws IOException
	 * @throws MyException
	 */
	public int delete_file(StorageClient1 client,String fileid)
			{
		try {
			return client.delete_file1(fileid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 99;
	}

	
	
	
	/**
	* download file from storage server
	* @param group_name the group name of storage server
	* @param remote_filename filename on storage server
	* @param local_filename  filename on local
	* @return 0 success, return none zero errno if fail
	*/
	public int download_file(StorageClient1 client,String group_name, String remote_filename,
			String local_filename) throws IOException, MyException {
		return client.download_file(group_name, remote_filename, 0, 0,
				local_filename);
	}
	

	/**
	 * 
	 * @param fileid
	 * @return
	 */
	private FileInfo fetchFileInfo(StorageClient1 client,String fileid) throws Exception{
 		return client.get_file_info1(fileid);
	}
	

	/**
	 * 分配服务端的链接
	 * 
	 * @throws MyException
	 */
	private StorageClient1 assignResourse(String fileid) throws MyException {
		
		StorageClient1 client = new StorageClient1();

		try {
			if (client.getTrackerServer() != null) {
				logger.debug("active test to tracker server: "
						+ client.getTrackerServer().getSocket().isClosed());
			}
			if (client.getStorageServer() != null) {
				logger.debug("active test to storage server: "
						+ client.getStorageServer().getSocket().isClosed());
			}

			if (client.getTrackerServer() == null
					|| (client.getTrackerServer() != null && client
							.getTrackerServer().getSocket().isClosed())) {
				TrackerServer trackerServer = tracker.getConnection();
				if(trackerServer == null ){
					throw new MyException();
				}
				client.setTrackerServer(trackerServer);
			}

			if (client.getStorageServer() == null
					|| (client.getStorageServer() != null && client
							.getStorageServer().getSocket().isClosed())) {
				StorageServer storageServer = null;
				if (fileid != null && fileid.length() > 0) {
					int pos = fileid
							.indexOf(StorageClient1.SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR);
					storageServer = tracker.getFetchStorage(
							client.getTrackerServer(), fileid.substring(0, pos),
							fileid.substring(pos + 1));
				} else {
					storageServer = tracker.getStoreStorage(
							client.getTrackerServer(), null);
				}
				if(storageServer == null){
					releaseResourse(client);
					throw new MyException();
				}
				client.setStorageServer(storageServer);
			}

			logger.debug("分配与服务端连接资源：client->"+client+",storageServer->" + client.getStorageServer()
					+ ",trackerServer->" + client.getTrackerServer());
			return client;

		} catch (Exception e) {
			throw new MyException("连接服务端失败");
		}
	}
	
	/**
	 * 释放服务端的链接
	 */
	private void releaseResourse(StorageClient1 client){
		try {
			if(client != null){
				logger.debug("释放与服务端连接资源：client->"+client+",storageServer->" + client.getStorageServer()
						+ ",trackerServer->" + client.getTrackerServer());
				
				if(client.getStorageServer() != null){
					client.getStorageServer().close();
				}
				
				if(client.getTrackerServer() != null ){
					client.getTrackerServer().close();
				}
			
				client.setTrackerServer(null);
				client.setStorageServer(null); 
				client = null;
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * 
	 * @param file_buff
	 *            file 文件内容
	 * @param file_ext_name
	 *            文件类型不包含. 如 txt doc jpg
	 * @return fileid if success:<br>
	 *         return null if fail
	 * @throws IOException
	 * @throws MyException
	 */
	public String upload_file(StorageClient1 client,byte[] file_buff, String file_ext_name)
			throws Exception {
		try {
			assignResourse(null);
			return client.upload_file1(file_buff, file_ext_name,
					new NameValuePair[] {});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			releaseResourse(client);
		}
		return null;
	}

}


