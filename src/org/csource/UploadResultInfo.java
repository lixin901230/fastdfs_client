/**
 * @desc 封装上传结果信息
 * @author ma
 * @date 2015年6月15日
 * 
 */
package org.csource;


public class UploadResultInfo {
	/**
	 * 上传服务器后文件标识，通过该fileid能够下载或续传文件
	 */
	private String fileid ;
	/**
	 * 文件是否完整成功上传
	 */
	private boolean flag = false;
	/**
	 * 本机上传文件路径：绝对路径，如果通过字节数组上传该值为null
	 */
	private String localfilepath;
	/**
	 * 上传信息，一般用来描述异常信息
	 */
	private String message;
	/**
	 * 本机标识已上传文件信息，如果异常中断一般实际服务端文件大小比本机记录的要大
	 */
	private long size;
	/**
	 * http下载地址
	 */
	private String url;
	/**
	 * http备用下载地址列表
	 */
	private String bakurl;
	
	public String getFileid() {
		return fileid;
	}
	public void setFileid(String fileid) {
		this.fileid = fileid;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public String getLocalfilepath() {
		return localfilepath;
	}
	public void setLocalfilepath(String localfilepath) {
		this.localfilepath = localfilepath;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	
	
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public String getBakurl() {
		return bakurl;
	}
	public void setBakurl(String bakurl) {
		this.bakurl = bakurl;
	}
	public UploadResultInfo(String localfilepath, String message) {
		super();
		this.localfilepath = localfilepath;
		this.message = message;
	}
	
	public UploadResultInfo(String fileid, String localfilepath, String message) {
		super();
		this.fileid = fileid;
		this.localfilepath = localfilepath;
		this.message = message;
	}
	
	
	public UploadResultInfo(String fileid, String localfilepath,
			String message, long size) {
		super();
		this.fileid = fileid;
		this.localfilepath = localfilepath;
		this.message = message;
		this.size = size;
	}
	public UploadResultInfo(String fileid, boolean flag, String localfilepath,
			String message, long size) {
		super();
		this.fileid = fileid;
		this.flag = flag;
		this.localfilepath = localfilepath;
		this.message = message;
		this.size = size;
	}
	
	
	
	public UploadResultInfo(String fileid, boolean flag, String localfilepath,
			String message, long size, String url) {
		super();
		this.fileid = fileid;
		this.flag = flag;
		this.localfilepath = localfilepath;
		this.message = message;
		this.size = size;
		this.url = url;
	}
	@Override
	public String toString() {
		return "UploadResultInfo [fileid=" + fileid + ", flag=" + flag
				+ ", localfilepath=" + localfilepath + ", message=" + message
				+ ", size=" + size + ", url=" + url + ", bakurl=" + bakurl
				+ "]";
	}
	
	
}


