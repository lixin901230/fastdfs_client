/**
 * @desc
 * @author ma
 * @date 2015年6月3日
 * 
 */
package test;

import java.io.InputStream;

public class WatchThread implements Runnable{
	private InputStream is;
	
	public WatchThread(InputStream is) {
		super();
		this.is = is;
	}

	@Override
	public void run() {
		try {
			byte[] size = new byte[2];
			int num ;
			while((num = is.read(size,0,2)) < 0){
				System.out.println(num);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}


