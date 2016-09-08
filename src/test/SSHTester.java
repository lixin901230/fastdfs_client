/**
 * 
 */
package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.taobao.taokeeper.common.constant.SystemConstant;

import common.toolkit.java.exception.IllegalParamException;
import common.toolkit.java.exception.SSHException;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.number.IntegerUtil;

/**
 * @author Administrator
 *
 */
public class SSHTester {

	/**
	 * @param args
	 * @throws SSHException 
	 */
	public static void main(String[] args) throws SSHException {
		// TODO Auto-generated method stub
		List list = execute("172.16.21.185",SystemConstant.portOfSSH,"root","123.com","cd /home/storage/data/00/00;ls ");
		System.out.println(list);
	}
	
	/**
	 * SSH 方式登录远程主机，执行命令,方法内部会关闭所有资源，调用方无须关心。
	 * @param ip 主机ip
	 * @param username 用户名
	 * @param password 密码
	 * @param command 要执行的命令
	 */
	public static List<String> execute( String ip, int port, String username, String password, String command ) throws SSHException {

		if ( StringUtil.isBlank( command ) )
			return null;
		port = IntegerUtil.defaultIfSmallerThan0( port, 22 );
		Connection conn = null;
		Session session = null;
		BufferedReader read = null;
		StringBuffer sb = new StringBuffer();
		try {
			if ( StringUtil.isBlank( ip ) ) {
				throw new IllegalParamException( "Param ip is empty!" );
			}
			username = StringUtil.defaultIfBlank( username, SystemConstant.userNameOfSSH );
			password = StringUtil.defaultIfBlank( password, SystemConstant.passwordOfSSH );
			conn = new Connection( ip, port );
 			conn.connect( null, 2000, 2000 );
			boolean isAuthenticated = conn.authenticateWithPassword( username, password );
			if ( isAuthenticated == false ) {
				throw new Exception( "SSH authentication failed with [ userName: " + username + ", password: " + password + "]" );
			}

			session = conn.openSession();
			session.execCommand( command );

			read = new BufferedReader( new InputStreamReader( new StreamGobbler( session.getStdout() ) ) );
			String line = "";
			List<String> list = new ArrayList<String>();
			while ( ( line = read.readLine() ) != null ) {
				sb.append( line ).append("\n");
				list.add(line);
			}
			return list;
		} catch ( Exception e ) {
			throw new SSHException( "SSH远程执行command: " + command + " 出现错误: " + e.getMessage(), e );
		} finally {
			if ( null != read ) {
				try {
					read.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
			if ( null != session ) {
				session.close();
			}
			if ( null != conn ) {
				conn.close();
			}
		}
	}
}
