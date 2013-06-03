package getData;
import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * get NBA data from 
 * <a herf="http://sports.sina.com.cn/nba/">SINA NBA website</a>
 * and insert into database
 * 
 * @author gubingchuan
 * 
 */
public class GetData {
	private final static String CRLF = System.getProperty("line.separator");
	/**
	 * @param url  address
	 * @return HTML code
	 */
	private static String getStr(String url){
		try {
			URL ur = new URL(url);
			InputStream instr = ur.openStream();
			String s ;//
			BufferedReader in = new BufferedReader(new InputStreamReader(instr));
			StringBuffer sb = new StringBuffer();
			while ((s = in.readLine()) != null) {
				sb.append(s + CRLF);
			}	
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "MalformedURLException";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
	/**
	 *@param gameid 
	 *@return if this gameid is availabe then return the HTML code for this game,else return "No Data!"
	 * */
	private static String getHTML(String gameid){
		String add="http://nba.sports.sina.com.cn/look_scores.php?id="+gameid;
		//s is html code
		String s=getStr(add);
		if(s.length()<=30000){
			return "No Data!";
		}else {
			return s;
		}
	}
	/**
	 * @param s  A game's HTML code
	 *@return if the HTML code is available ,
	 *then return a ArrayList that contains SQL for inserting data!
	 *else return "No Data!"
	 * */
	private static ArrayList<String> getQuarter(String s){
		ArrayList<String> sql=new ArrayList<String>();
		if(s.length()<=30000){
			sql.add("No Data!");
			return sql;
		}else{
			//get gameid
			int gameidStart=s.indexOf("frameborder=\"0\"></iframe>");
			gameidStart=gameidStart-17;
			String gameid=s.substring(gameidStart,gameidStart+10);
		//scoreStart
		int scs=s.indexOf("<tr align=center><td bgcolor=\"#000000\">");
		//scoreEnd
		int sce=s.indexOf("</tr>",scs);
		//get the bar of score
		String guestScoreTable=s.substring(scs,sce);
		ArrayList<String> guestScore=new ArrayList<String>();
		int q=guestScoreTable.indexOf("</td>");
		// the number in front of </td> is the score for one quarter
		int i=0;
		while(q!=-1){
			if(guestScoreTable.substring(q-2,q-1).equals(">")){
				guestScore.add(guestScoreTable.substring(q-1,q));
			}else{
				guestScore.add(guestScoreTable.substring(q-2,q));
			}
		i++;
		q=guestScoreTable.indexOf("</td>",q+6);
		}
		scs=s.indexOf("<tr align=center><td bgcolor=\"#000000\">",sce);
		//scoreEnd
		sce=s.indexOf("</tr>",scs);
		String hostScoreTable=s.substring(scs,sce);
		ArrayList<String> hostScore=new ArrayList<String>();
		q=hostScoreTable.indexOf("</td>");
		// the number in front of </td> is the score for one quarter
		i=0;
		while(q!=-1){
		 /*hostScore[i]=hostScoreTable.substring(q-2,q);
		if(hostScore[i].substring(0,1).equals(">")){
			hostScore[i]=hostScore[i].substring(1,2);
			}*/
			if(hostScoreTable.substring(q-2,q-1).equals(">")){
				hostScore.add(hostScoreTable.substring(q-1,q));
			}else{
				hostScore.add(hostScoreTable.substring(q-2,q));
			}
		i++;
		q=hostScoreTable.indexOf("</td>",q+6);
		}
		
		
		for(i=0;i<guestScore.size();i++){
			String tmpSql="insert into quarter_score values(";
			tmpSql=tmpSql+gameid+",";
			tmpSql=tmpSql+"false"+",";
			tmpSql=tmpSql+String.valueOf(i+1)+",";
			tmpSql=tmpSql+guestScore.get(i)+")";
			sql.add(tmpSql);
		}
		for(i=0;i<hostScore.size();i++){
			String tmpSql="insert into quarter_score values(";
			tmpSql=tmpSql+gameid+",";
			tmpSql=tmpSql+"true"+",";
			tmpSql=tmpSql+String.valueOf(i+1)+",";
			tmpSql=tmpSql+hostScore.get(i)+")";
			sql.add(tmpSql);
		}
		for (i=0;i<sql.size();i++){
		System.out.println(sql.get(i));
		}
		return sql;
		
		}	
	}
	
	
	/**
	 * @param s HTML code for showing the data
	 * @return if the data is availabe, then return the insert sql for table game
	 * 
	 * */
	private static String getGame(String s){
		if(s.length()<=30000){
			return "No Data!";
		}else{
        //get gameid
		int gameidStart=s.indexOf("frameborder=\"0\"></iframe>");
		gameidStart=gameidStart-17;
		String gameid=s.substring(gameidStart,gameidStart+10);
		
		//get the ID of host team and guest team
		int guestTeamStart=s.indexOf("team.php?id=");
		guestTeamStart=guestTeamStart+12;
		int guestTeamEnd=guestTeamStart+2;	
		int hostTeamStart=s.indexOf("team.php?id=",guestTeamStart);
		hostTeamStart=hostTeamStart+12;
		hostTeamStart=s.indexOf("team.php?id=",hostTeamStart);
		hostTeamStart=hostTeamStart+12;
		int hostTeamEnd=hostTeamStart+2;
		String guest=s.substring(guestTeamStart, guestTeamEnd);
		String host=s.substring(hostTeamStart, hostTeamEnd);
		//System.out.println("gameid: "+gameid);
		if(guest.substring(1,2).equals("\"")){
			guest=guest.substring(0,1);
		}
		if(host.substring(1,2).equals("\"")){
			host=host.substring(0,1);
		}
		int guestScoreStart=s.indexOf("class=\"num\">")+12;
		int guestScoreEnd=s.indexOf("</td>",guestScoreStart);
		String guestScore=s.substring(guestScoreStart,guestScoreEnd);
		int hostScoreStart=s.indexOf("class=\"num\">",guestScoreStart)+12;
		int hostScoreEnd=s.indexOf("</td>",hostScoreStart);
		String hostScore=s.substring(hostScoreStart,hostScoreEnd);
		
				String sql="insert into game values(";
				sql=sql+gameid+","+guest+","+host+","+guestScore+","+hostScore+");";
				System.out.println(sql);
         
		return sql;
		}}
	/**
	 * @param s HTML code for showing the data
	 * @return if the data is availabe, 
	 * then return the insert SQL of all players for table scores
	 * 
	 * */
	private static String getScore(String s){
		if(s.length()<=30000){
			return "No Data!";
			//System.out.println("No Data!");
		}else{
			int strStart=s.indexOf("frameborder=\"0\"></iframe>");
			strStart=strStart-17;
			String gameid=s.substring(strStart,strStart+10);
			
			//get personal data
			int pdStart=s.indexOf("<td height=\"20\"><a");
			int pdEnd=s.indexOf("<td height=\"20\"><",pdStart+1);
			
			int dist=0;
			String sql="";
			String isstart="true";
			String ishost="false";
			String pd;
			while(pdEnd!=-1){
			pd=s.substring(pdStart,pdEnd);
			sql=sql+GetData.getPersonalData(gameid,pd,isstart,ishost);
			dist=pdEnd-pdStart;
			if(dist>400&&dist<800){
				isstart="false";
			}else if(dist>800){
				isstart="true";
				ishost="true";
			}
			pdStart=pdEnd;
			pdEnd=s.indexOf("<td height=\"20\"><",pdStart+1);			
			}
			pdEnd=s.indexOf("<tr",pdStart);
			pd=s.substring(pdStart,pdEnd);
			sql=sql+GetData.getPersonalData(gameid,pd,isstart,ishost);
			//System.out.println(sql);
			return sql;
		}
	}
	
	
	/**
	 * @param gameid  HTML code for showing personal data
	 * @return if the data is available, return the sql for this player,else return "No Data!"
	 * */
	
	private static String getPersonalData(String gameid,String html,String isstart,String ishost){
		int p1Start=html.indexOf("player_one.php?id=");
		p1Start=p1Start+"player_one.php?id=".length();
		int p1End=html.indexOf("\"",p1Start);
		//int p1End=p1Start+4;
		String p1=html.substring(p1Start,p1End);
		//p1 is playerID
		int p1NameStart=html.indexOf(">",p1End)+4;
		p1NameStart=p1NameStart+1;
		int p1NameEnd=html.indexOf("<",p1NameStart);
		String p1Name=html.substring(p1NameStart,p1NameEnd);
		//p1Name is player's chinese name
		int p1MinStart=html.indexOf("<td>",p1NameEnd);
		//for the players which was not attending
		if(p1MinStart==-1||p1MinStart-p1NameEnd>20){
			String sql="insert into scores values("+gameid;//gameID
			sql=sql+","+p1;//player ID
			sql=sql+",\""+p1Name;
			sql=sql+"\",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
			sql=sql+","+isstart;//is  starting lineup ?
			sql=sql+","+ishost;//is hostteam?
			sql=sql+"); ";
			//System.out.println(sql);
			return sql;
		}else{
		p1MinStart=p1MinStart+4;
		int p1MinEnd=html.indexOf("</td>",p1MinStart);
		String p1Min=html.substring(p1MinStart,p1MinEnd);
		//p1Min is the playing time for a player
		int p1FG_MZStart=html.indexOf("<td>",p1MinEnd);
		p1FG_MZStart= p1FG_MZStart+4;
		
		int p1FG_MZEnd=html.indexOf("-",p1FG_MZStart);
		String p1FG_MZ=html.substring(p1FG_MZStart,p1FG_MZEnd);
		//p1FG_MZ number of field goal
		int p1FGStart=p1FG_MZEnd+1;
		int p1FGEnd=html.indexOf("</td>",p1FGStart);
		String p1FG=html.substring(p1FGStart,p1FGEnd);
		//p1FG number of shooting
		int p1FG3_MZStart=html.indexOf("<td>",p1FGEnd)+4;
		int p1FG3_MZEnd=html.indexOf("-",p1FG3_MZStart);
		String p1FG3_MZ=html.substring(p1FG3_MZStart,p1FG3_MZEnd);
		//p1FG3_MZ number of 3 points goal
		int p1FG3Start=p1FG3_MZEnd+1;
		int p1FG3End=html.indexOf("</td>",p1FG3Start);
		String p1FG3=html.substring(p1FG3Start,p1FG3End);
		//p1FG3 number of 3 points shooting
		int p1FT_MZStart=html.indexOf("<td>",p1FG3End)+4;
		int p1FT_MZEnd=html.indexOf("-",p1FT_MZStart);
		String p1FT_MZ=html.substring(p1FT_MZStart,p1FT_MZEnd);
		//p1FT_MZ number of free throw goal
		int p1FTStart=p1FT_MZEnd+1;
		int p1FTEnd=html.indexOf("</td>",p1FTStart);
		String p1FT=html.substring(p1FTStart,p1FTEnd);
		//p1FT number of free throw shooting
		
		String[] stat=new String[9];
		//the 9 Strings are for oreb, dreb, reb, ast, stl, blk,to,pf,ps
		int indexStart=0;
		int indexEnd=p1FTEnd;
		for(int i=0;i<9;i++){
			indexStart=html.indexOf("<td>",indexEnd)+4;
			indexEnd=html.indexOf("</td>",indexStart);
			
			stat[i]=html.substring(indexStart,indexEnd);
			//System.out.println(stat[i]);
		}
		String sql="insert into scores values("+gameid;//gameID
		sql=sql+","+p1;//playerID
		sql=sql+",\""+p1Name;
		sql=sql+"\","+p1Min;
		sql=sql+","+p1FG;
		sql=sql+","+p1FG_MZ;
		sql=sql+","+p1FG3;
		sql=sql+","+p1FG3_MZ;
		sql=sql+","+p1FT;
		sql=sql+","+p1FT_MZ;
		for(int i=0;i<9;i++){
			sql=sql+","+stat[i];
		}
		sql=sql+","+isstart;//is startin lineup
		sql=sql+","+ishost;// is hostteam
		sql=sql+"); ";
		//System.out.println(sql);
		return sql;
		}
	}
	
	
	
	/**
	 * @param beginDate
	 * @param endDate
	 * Get the data from between begin date and end date<br>
	 * and insert it into database
     * The data format is :
	 * String beginDate = "20110927";
	 * String endDate="20130528";
	 * */
	public static void getGamesData(String beginDate,String endDate){

				DateFormat format1 = new SimpleDateFormat("yyyyMMdd");
				
				JDBC j=new JDBC();
				//delete data before insert
				String deleteSql="delete from scores where substr(gameid,1,length(gameid)-2)<=";
				deleteSql=deleteSql+endDate;
				deleteSql=deleteSql+" and substr(gameid,1,length(gameid)-2)>=";
				deleteSql=deleteSql+beginDate;
				j.updateDB(deleteSql);//delete scores table
				System.out.println("Score has been deleted!");
				deleteSql="delete from game where substr(gameid,1,length(gameid)-2)<=";
				deleteSql=deleteSql+endDate;
				deleteSql=deleteSql+" and substr(gameid,1,length(gameid)-2)>=";
				deleteSql=deleteSql+beginDate+";";
				j.updateDB(deleteSql);//delete game table
				System.out.println("Game has been deleted!");
				deleteSql="delete from quarter_score where substr(gameid,1,length(gameid)-2)<=";
				deleteSql=deleteSql+endDate;
				deleteSql=deleteSql+" and substr(gameid,1,length(gameid)-2)>=";
				deleteSql=deleteSql+beginDate+";";
				j.updateDB(deleteSql);//delete quarter_score table
				System.out.println("quarter_score has been deleted!");
				while(Integer.parseInt(beginDate)<=Integer.valueOf(endDate)){
					for (int h=1;h<=30;h++){
						String hh=null;
						if (h<10){
							hh="0"+h;
						}else{hh=String.valueOf(h);}
						String gameid=beginDate+hh;
						String strHTML=getHTML(gameid);
						String sql=getGame(strHTML);//get data for one game
						//String sql=getScore(strHTML);
						sql=sql+getScore(strHTML);//get the data for one player
						if(sql.contains("No Data!")){
							
						}else{
							String ssql="";
							int sqlStart=0;
							int sqlEnd=sql.indexOf(";");
							while(sqlEnd!=-1){
								ssql=sql.substring(sqlStart,sqlEnd+1);
								System.out.println(ssql);
								j.updateDB(ssql);
								sqlStart=sqlEnd+1;
								sqlEnd=sql.indexOf(";",sqlStart);
							}
							ArrayList<String> quarterScore=getQuarter(strHTML);
							for(int i=0;i<quarterScore.size();i++){
								j.updateDB(quarterScore.get(i));
								System.out.println(quarterScore.get(i));
							}
							System.out.println("gameID:"+ssql.substring(27, 27+10)+" imported!");
						}
					}
					//date add 1
					Calendar c = Calendar.getInstance();
					Date date;
					try {
						date = format1.parse(beginDate);
						c.setTime(date); 
						c.add(c.DATE,1);
						date=c.getTime();   //the next day 
						beginDate=format1.format(date);
						//System.out.println(str);
					} catch (ParseException e){
						e.printStackTrace();
					}
				}
	}

	
	
    /**get player's information from 
     * http://nba.sports.sina.com.cn/players.php 
     * http://nba.sports.sina.com.cn/player.php?id=3975
     * 
     * */
	public static void getPlayers(){
		JDBC j=new JDBC();
		/*
		 * get the div
		 * */
		String html=getStr("http://nba.sports.sina.com.cn/players.php");
		int strStart=html.indexOf("<div id=\"table980middle\">");
		int strEnd=html.indexOf("<div id=\"table980bottom\"></div>",strStart);
		html=html.substring(strStart,strEnd);
		//System.out.println(html);
		//get one team
		int teamEnd=0;
		
		while(html.indexOf("team.php",teamEnd)>=0){
			int teamStart=html.indexOf("team.php",teamEnd);
			teamEnd=html.indexOf(CRLF,html.indexOf(CRLF,teamStart)+1);			
			String strTeam=html.substring(teamStart,teamEnd);
			//System.out.println(strTeam);
			//teamID
			strStart=strTeam.indexOf("team.php?id")+12;
			strEnd=strTeam.indexOf("\"");
			String teamid=strTeam.substring(strStart,strEnd);
			//get playerid	
			//System.out.println(teamid);
			
			while(strTeam.indexOf("player.php?id",strEnd)>=0){
				strStart=strTeam.indexOf("player.php?id",strEnd)+14;
				strEnd=strTeam.indexOf("'",strStart);
				String playerid=strTeam.substring(strStart,strEnd);
				
				String playerStr=getStr("http://nba.sports.sina.com.cn/player.php?id="+playerid);
				playerStr=playerStr.substring(playerStr.indexOf("<div id=\"table730top\">"),playerStr.indexOf("</div></div>",playerStr.indexOf("<div id=\"table730top\">")));
				int playerStart=playerStr.indexOf("x;\">")+4;
				int playerEnd=playerStr.indexOf("</strong>");
				String name_ch=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("(",playerEnd)+1;
				playerEnd=playerStr.indexOf(")",playerStart);
				String name_en=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("</a> | ",playerEnd)+6;
				playerEnd=playerStr.indexOf("号",playerStart);
				String number=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("|",playerEnd)+1;
				playerEnd=playerStr.indexOf("|",playerStart);
				String position=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("width=197>",playerEnd)+10;
				playerEnd=playerStr.indexOf("</td>",playerStart);
				String birthday=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("学校</td>",playerEnd)+14;
				playerEnd=playerStr.indexOf("</td>",playerStart);
				String grt_schl=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("高</td><td>",playerEnd)+13;
				playerEnd=playerStr.indexOf("米",playerStart);
				String height=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("重</td><td>",playerEnd)+13;
				playerEnd=playerStr.indexOf("公斤",playerStart);
				String weight=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("进入 NBA</td>",playerEnd)+18;
				playerEnd=playerStr.indexOf("年</td>",playerStart);
				String init_nba=playerStr.substring(playerStart,playerEnd);
				playerStart=playerStr.indexOf("选秀情况",playerEnd+1)+26;
				playerEnd=playerStr.indexOf("</td>",playerStart);
				String xxsw=playerStr.substring(playerStart,playerEnd);
				String sql="insert into players values(";
				sql=sql+playerid+",\"";
				sql=sql+name_ch+"\",\"";
				sql=sql+name_en+"\",";
				sql=sql+teamid+",";
				sql=sql+number+",\"";
				sql=sql+position+"\",";
				sql=sql+weight+",";
				sql=sql+height+",\"";
				sql=sql+grt_schl+"\",\"";
				sql=sql+birthday+"\",";
				if(init_nba.equals("")){
					sql=sql+"null,\"";
				}else{
					sql=sql+init_nba+",\"";
				}
			    
				sql=sql+xxsw+"\")";
				
				//System.out.println(playerStart);
				//System.out.println(playerEnd);
				System.out.println(sql);
				try{
					j.updateDB(sql);
				}catch(Exception e){
					break;
				}
				
				//System.out.println(playerStr);
			}
		}		
	}
	/**
	 * get the information of player which is not appear in the list of
	 * http://nba.sports.sina.com.cn/players.php 
	 * */
	public static void getPlayersFromScore(){
		JDBC j=new JDBC();
		String sql="select aa.playerid,case aa.ishost when true then bb.HOSTTEAM else bb.GUESTTEAM end as teamid from (select distinct a.playerid,max(a.GAMEID) as gameid,a.ISHOST as ishost from scores a left join players b using (playerid) where b.playerid is null group by a.PLAYERID) aa,game bb where aa.gameid=bb.GAMEID";
		ArrayList<String[]> players=j.selectTwoColumns(sql);
		//先删再插
		for(int i=0;i<players.size();i++){
			String deletesql="delete from players where playerid="+players.get(i)[0];
			j.updateDB(deletesql);
			System.out.println("player "+players.get(i)[0]+" deleted!");
		}
		for(int i=0;i<players.size();i++){
			String playerStr=getStr("http://nba.sports.sina.com.cn/player.php?id="+players.get(i)[0]);
			playerStr=playerStr.substring(playerStr.indexOf("<div id=\"table730top\">"),playerStr.indexOf("</div></div>",playerStr.indexOf("<div id=\"table730top\">")));
			//System.out.println(playerStr);
			int playerStart=playerStr.indexOf("x;\">")+4;
			int playerEnd=playerStr.indexOf("</strong>");
			String name_ch=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("(",playerEnd)+1;
			playerEnd=playerStr.indexOf(")",playerStart);
			String name_en=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("</a> | ",playerEnd)+6;
			playerEnd=playerStr.indexOf("号",playerStart);
			String number=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("|",playerEnd)+1;
			playerEnd=playerStr.indexOf("|",playerStart);
			String position=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("width=197>",playerEnd)+10;
			playerEnd=playerStr.indexOf("</td>",playerStart);
			String birthday=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("学校</td>",playerEnd)+14;
			playerEnd=playerStr.indexOf("</td>",playerStart);
			String grt_schl=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("高</td><td>",playerEnd)+13;
			playerEnd=playerStr.indexOf("米",playerStart);
			String height=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("重</td><td>",playerEnd)+13;
			playerEnd=playerStr.indexOf("公斤",playerStart);
			String weight=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("进入 NBA</td>",playerEnd)+18;
			playerEnd=playerStr.indexOf("年</td>",playerStart);
			String init_nba=playerStr.substring(playerStart,playerEnd);
			playerStart=playerStr.indexOf("选秀情况",playerEnd+1)+26;
			playerEnd=playerStr.indexOf("</td>",playerStart);
			String xxsw=playerStr.substring(playerStart,playerEnd);
			String insertsql="insert into players values(";
			insertsql=insertsql+players.get(i)[0]+",\"";
			insertsql=insertsql+name_ch+"\",\"";
			insertsql=insertsql+name_en+"\",";
			insertsql=insertsql+players.get(i)[1]+",";
			if(number.equals(" ")){
				insertsql=insertsql+"null,\"";
			}else{
				insertsql=insertsql+number+",\"";
			}
			insertsql=insertsql+position+"\",";
			insertsql=insertsql+weight+",";
			insertsql=insertsql+height+",\"";
			insertsql=insertsql+grt_schl+"\",\"";
			insertsql=insertsql+birthday+"\",";
			if(init_nba.equals("")){
				insertsql=insertsql+"null,\"";
			}else{
				insertsql=insertsql+init_nba+",\"";
			}
		    
			insertsql=insertsql+xxsw+"\")";
			
			//System.out.println(playerStart);
			//System.out.println(playerEnd);
			System.out.println(insertsql);
			j.updateDB(insertsql);
		}
		System.out.println("players added complete!");
	}
	
	public static void main(String[] agrs){
		
        //String s=t1.getHTML("2010101713");
        //getQuarter(s);
        //getGame(s);
        //getGamesData("20080910","20130602");//
		//getPlayers();//
		getPlayersFromScore();
		//String sql="select * from players where playerid=2625";
		
	}
}
