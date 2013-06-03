package getData;
import java.sql.*;   
import java.util.ArrayList;
public class JDBC {   
	static String driver = "com.mysql.jdbc.Driver";
	static String url = "jdbc:mysql://127.0.0.1:3306/nbadata?useUnicode=true&characterEncoding=utf8";
	static String user = "root";
	static String password = "123456";
public void updateDB(String sql){  
	try {
		// load the driver
		Class.forName(driver);
		//connecting MySQL database
		Connection conn = DriverManager.getConnection(url, user, password);
		if(!conn.isClosed()){
		//System.out.println("Succeeded connecting to the Database!");
		Statement statement = conn.createStatement();
		//sql=new String(sql.getBytes("GB2312"),"ISO-8859-1");
		statement.executeUpdate(sql);
		//System.out.println("update success!");  
		conn.close();
		
		}else{
			System.out.println("connection is not opened");
		}
	
	}catch(ClassNotFoundException e) {   
		System.out.println("Sorry,can`t find the Driver!");   
		e.printStackTrace();   
		} catch(SQLException e) {   
		e.printStackTrace();   
		} catch(Exception e) {   
		e.printStackTrace();   
		}   
	
}
/**
 * @param sql for two fields
 * @return ArrayList<String[]>
 * 
 * */
public ArrayList<String[]> selectTwoColumns(String sql){  
	ArrayList<String[]> re=new ArrayList<String[]>();
	try {
		// load driver
		Class.forName(driver);
		// connect database
		Connection conn = DriverManager.getConnection(url, user, password);
		
		if(!conn.isClosed()){
		//System.out.println("Succeeded connecting to the Database!");
		Statement statement = conn.createStatement();
		//sql=new String(sql.getBytes("GB2312"),"ISO-8859-1");
		ResultSet rs=statement.executeQuery(sql);
		
		while(rs.next()){
			String[] tmp=new String[2];
			tmp[0]=rs.getString(1);
			tmp[1]=rs.getString(2);
			re.add(tmp);
			//System.out.println(rs.getString(1));
		}
		conn.close();
		
		}else{
			System.out.println("connection is not opened");
		}
	
	}catch(ClassNotFoundException e) {   
		System.out.println("Sorry,can`t find the Driver!");   
		e.printStackTrace();   
		} catch(SQLException e) {   
		e.printStackTrace();   
		} catch(Exception e) {   
		e.printStackTrace();   
		}
	return re;   
	
}
}